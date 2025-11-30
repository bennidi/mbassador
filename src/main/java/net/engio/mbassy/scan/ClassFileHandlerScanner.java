package net.engio.mbassy.scan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.Annotation;
import java.lang.classfile.AttributedElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans the classpath/directories using the JDK 24+ Class-File API.
 * It finds classes containing methods annotated with @Handler without
 * loading the class first.
 */
public class ClassFileHandlerScanner {

  private static final String HANDLER_DESC = "Lnet/engio/mbassy/listener/Handler;";

  /**
   * Scans the given package for classes that contain methods annotated with @Handler.
   *
   * @param packageName The package to scan (e.g. "com.example.handlers")
   * @return A list of loaded Classes that have handlers.
   * @throws IOException If scanning fails.
   */
  public List<Class<?>> scanPackage(String packageName) throws IOException {
    try {
      // 1. Discovery Phase: Find all potential .class files in the package (Dir or JAR)
      Set<String> classNames = discoverClassNames(packageName);

      // 2. Analysis Phase: Read bytes, check for annotation, and load matches
      return analyzeClasses(classNames);
    } catch (Exception e) {
      throw new IOException("Failed to scan package: " + packageName, e);
    }
  }

  private List<Class<?>> analyzeClasses(Set<String> classNames) {
    List<Class<?>> matches = new ArrayList<>();

    for (String className : classNames) {
      try {
        // Read bytes via ClassLoader - works for both file system and JARs
        byte[] classBytes = readClassBytes(className);
        if (classBytes == null) continue;

        ClassModel classModel = ClassFile.of().parse(classBytes);

        // Check if any method has the @Handler annotation
        boolean hasHandler = classModel.methods().stream()
            .anyMatch(method -> hasAnnotation(method, HANDLER_DESC));

        if (hasHandler) {
          // Only load the class into the JVM if it actually has handlers
          matches.add(Class.forName(className));
        }
      } catch (Throwable e) {
        // Ignore classes that cannot be loaded or parsed (e.g., missing dependencies)
        // System.err.println("Skipping " + className + ": " + e.getMessage());
      }
    }
    return matches;
  }

  /**
   * Helper to check for a specific annotation descriptor on an element (Method or Class).
   * Handles both RuntimeVisible and RuntimeInvisible attributes.
   */
  private boolean hasAnnotation(AttributedElement element, String descriptor) {
    for (var attr : element.attributes()) {
      List<Annotation> annotations = null;
      if (attr instanceof RuntimeVisibleAnnotationsAttribute rva) {
        annotations = rva.annotations();
      } else if (attr instanceof RuntimeInvisibleAnnotationsAttribute ria) {
        annotations = ria.annotations();
      }

      if (annotations != null) {
        for (Annotation a : annotations) {
          if (a.className().stringValue().equals(descriptor)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private byte[] readClassBytes(String className) throws IOException {
    String resourceName = className.replace('.', '/') + ".class";
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
      if (is == null) return null;
      return is.readAllBytes();
    }
  }

  private Set<String> discoverClassNames(String basePackage) throws IOException, URISyntaxException {
    Set<String> classNames = new HashSet<>();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = basePackage.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);

    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      if ("file".equals(resource.getProtocol())) {
        classNames.addAll(findClassesInDirectory(basePackage, Paths.get(resource.toURI())));
      } else if ("jar".equals(resource.getProtocol())) {
        classNames.addAll(findClassesInJar(basePackage, resource));
      }
    }
    return classNames;
  }

  private Set<String> findClassesInDirectory(String basePackage, Path directory) throws IOException {
    Set<String> classes = new HashSet<>();
    if (!Files.isDirectory(directory)) return classes;

    // Create the path segment corresponding to the package (e.g., "com/example")
    String packagePathPart = basePackage.replace('.', File.separatorChar);

    try (var stream = Files.walk(directory)) {
      stream.filter(p -> p.toString().endsWith(".class"))
          .forEach(path -> {
            String fullPath = path.toString();
            // We need to extract the qualified name relative to the classpath root
            int packageStartIndex = fullPath.indexOf(packagePathPart);
            if (packageStartIndex != -1) {
              String className = fullPath
                  .substring(packageStartIndex)
                  .replace(File.separatorChar, '.')
                  .replace(".class", "");
              classes.add(className);
            }
          });
    }
    return classes;
  }

  private Set<String> findClassesInJar(String basePackage, URL jarUrl) throws IOException {
    Set<String> classes = new HashSet<>();
    JarURLConnection conn = (JarURLConnection) jarUrl.openConnection();
    String pathPrefix = basePackage.replace('.', '/') + "/";

    try (JarFile jar = conn.getJarFile()) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.startsWith(pathPrefix) && name.endsWith(".class") && !entry.isDirectory()) {
          classes.add(name.replace('/', '.').replace(".class", ""));
        }
      }
    }
    return classes;
  }
}