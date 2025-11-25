# Migration Guide: EL Expressions to Lambda-Based Filters

## Overview

MBassador has been modernized to use **type-safe, lambda-compatible filter classes** instead of string-based Expression Language (EL) conditions. This change brings:

- ✅ **Compile-time type safety** - No more runtime EL parsing errors
- ✅ **Better IDE support** - Auto-completion, refactoring, and debugging
- ✅ **Improved performance** - Direct method invocation vs. EL evaluation
- ✅ **Modern Java** - Leverages Java's functional interface capabilities
- ✅ **No external dependencies** - Removed Jakarta EL and JUEL dependencies
- ✅ **Easier testing** - Filter logic can be unit tested independently

## Breaking Changes

### Removed Features

1. **`condition` parameter in `@Handler` annotation** - No longer supported
2. **Jakarta EL and JUEL dependencies** - Removed from pom.xml
3. **ElFilter and StandardELResolutionContext classes** - Deleted

### Updated Features

- **`IMessageFilter` interface** - Now a `@FunctionalInterface` for lambda compatibility

## Migration Steps

### Step 1: Identify Handlers Using EL Conditions

Search your codebase for handlers using the `condition` parameter:

```bash
grep -r "condition\s*=" src/
```

### Step 2: Convert Each EL Expression to a Filter Class

For each handler with an EL condition, create a filter class implementing `IMessageFilter<T>`.

#### Example 1: Simple Property Check

**Before (EL):**
```java
@Handler(condition = "msg.size >= 10000")
public void handleLargeFile(File file) {
    // handle large files
}
```

**After (Lambda-compatible Filter):**
```java
@Handler(filters = @Filter(LargeFileFilter.class))
public void handleLargeFile(File file) {
    // handle large files
}

static class LargeFileFilter implements IMessageFilter<File> {
    @Override
    public boolean accepts(File file, SubscriptionContext context) {
        return file.length() >= 10000;
    }
}
```

#### Example 2: String Comparison

**Before (EL):**
```java
@Handler(condition = "msg.type == 'TEST'")
public void handleTestMessage(TestEvent message) {
    // handle test messages
}
```

**After (Lambda-compatible Filter):**
```java
@Handler(filters = @Filter(TestTypeFilter.class))
public void handleTestMessage(TestEvent message) {
    // handle test messages
}

static class TestTypeFilter implements IMessageFilter<TestEvent> {
    @Override
    public boolean accepts(TestEvent message, SubscriptionContext context) {
        return "TEST".equals(message.getType());
    }
}
```

#### Example 3: Complex Condition with Multiple Checks

**Before (EL):**
```java
@Handler(condition = "msg.size > 2 && msg.size < 4")
public void handleMediumSized(TestEvent message) {
    // handle medium sized messages
}
```

**After (Lambda-compatible Filter):**
```java
@Handler(filters = @Filter(MediumSizeFilter.class))
public void handleMediumSized(TestEvent message) {
    // handle medium sized messages
}

static class MediumSizeFilter implements IMessageFilter<TestEvent> {
    @Override
    public boolean accepts(TestEvent message, SubscriptionContext context) {
        int size = message.getSize();
        return size > 2 && size < 4;
    }
}
```

#### Example 4: Method Call in Condition

**Before (EL):**
```java
@Handler(condition = "msg.getType().equals('XYZ') && msg.getSize() == 1")
public void handleSpecificMessage(TestEvent message) {
    // handle specific messages
}
```

**After (Lambda-compatible Filter):**
```java
@Handler(filters = @Filter(SpecificMessageFilter.class))
public void handleSpecificMessage(TestEvent message) {
    // handle specific messages
}

static class SpecificMessageFilter implements IMessageFilter<TestEvent> {
    @Override
    public boolean accepts(TestEvent message, SubscriptionContext context) {
        return "XYZ".equals(message.getType()) && message.getSize() == 1;
    }
}
```

#### Example 5: String Method Check

**Before (EL):**
```java
@Handler(condition = "!msg.isEmpty()")
public void handleNonEmptyStrings(String msg) {
    // handle non-empty strings
}
```

**After (Lambda-compatible Filter):**
```java
@Handler(filters = @Filter(NonEmptyStringFilter.class))
public void handleNonEmptyStrings(String msg) {
    // handle non-empty strings
}

static class NonEmptyStringFilter implements IMessageFilter<String> {
    @Override
    public boolean accepts(String message, SubscriptionContext context) {
        return !message.isEmpty();
    }
}
```

### Step 3: Combine Multiple Filters

You can still use multiple filters on a single handler. They all must accept the message for the handler to be invoked.

**Before (EL with multiple checks):**
```java
@Handler(
    condition = "msg.type == 'TEST' && msg.size > 100",
    filters = @Filter(CustomFilter.class)
)
public void handleMessage(TestEvent msg) {
    // handler logic
}
```

**After (Multiple filters):**
```java
@Handler(filters = {
    @Filter(TestTypeFilter.class),
    @Filter(LargeSizeFilter.class),
    @Filter(CustomFilter.class)
})
public void handleMessage(TestEvent msg) {
    // handler logic
}

static class TestTypeFilter implements IMessageFilter<TestEvent> {
    @Override
    public boolean accepts(TestEvent message, SubscriptionContext context) {
        return "TEST".equals(message.getType());
    }
}

static class LargeSizeFilter implements IMessageFilter<TestEvent> {
    @Override
    public boolean accepts(TestEvent message, SubscriptionContext context) {
        return message.getSize() > 100;
    }
}
```

### Step 4: Reusable Filters with Custom Annotations

Create custom annotations for commonly used filters:

**Before (EL):**
```java
@Handler(condition = "msg.getClass() == Integer.class && msg > 0")
public void handlePositiveIntegers(Integer msg) {
    // handler logic
}
```

**After (Reusable annotation):**
```java
// Define the filter
static class PositiveIntegerFilter implements IMessageFilter<Integer> {
    @Override
    public boolean accepts(Integer message, SubscriptionContext context) {
        return message != null && message > 0;
    }
}

// Create custom annotation
@Retention(RetentionPolicy.RUNTIME)
@Handler(filters = @Filter(PositiveIntegerFilter.class))
@Target(ElementType.METHOD)
@interface HandlePositiveIntegers {}

// Use the annotation
@HandlePositiveIntegers
public void handlePositiveIntegers(Integer msg) {
    // handler logic
}
```

### Step 5: Update Enveloped Handlers

For handlers using `@Enveloped` with conditions, the filter should work with `MessageEnvelope`:

**Before (EL):**
```java
@Handler(condition = "msg.size >= 10000")
@Enveloped(messages = {HashMap.class, LinkedList.class})
public void handleLarge(MessageEnvelope envelope) {
    // handle large collections
}
```

**After (Filter on envelope):**
```java
@Handler(filters = @Filter(LargeCollectionFilter.class))
@Enveloped(messages = {HashMap.class, LinkedList.class})
public void handleLarge(MessageEnvelope envelope) {
    // handle large collections
}

static class LargeCollectionFilter implements IMessageFilter<MessageEnvelope> {
    @Override
    public boolean accepts(MessageEnvelope envelope, SubscriptionContext context) {
        Object msg = envelope.getMessage();
        if (msg instanceof Map) {
            return ((Map<?, ?>) msg).size() >= 10000;
        }
        if (msg instanceof Collection) {
            return ((Collection<?>) msg).size() >= 10000;
        }
        return false;
    }
}
```

### Step 6: Remove EL Dependencies

If you were explicitly including Jakarta EL or JUEL dependencies, you can now remove them from your `pom.xml`:

```xml
<!-- REMOVE THESE -->
<dependency>
    <groupId>jakarta.el</groupId>
    <artifactId>jakarta.el-api</artifactId>
</dependency>
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.el</artifactId>
</dependency>
<dependency>
    <groupId>de.odysseus.juel</groupId>
    <artifactId>juel-impl</artifactId>
</dependency>
<dependency>
    <groupId>de.odysseus.juel</groupId>
    <artifactId>juel-spi</artifactId>
</dependency>
```

## Best Practices

### 1. Co-locate Filters with Handlers

For handler-specific filters, define them as static nested classes:

```java
public class MyListener {

    @Handler(filters = @Filter(MySpecificFilter.class))
    public void handleMessage(MyMessage msg) {
        // handler logic
    }

    static class MySpecificFilter implements IMessageFilter<MyMessage> {
        @Override
        public boolean accepts(MyMessage message, SubscriptionContext context) {
            return message.isValid();
        }
    }
}
```

### 2. Create Reusable Filter Library

For commonly used filters, create a central filter library:

```java
public class CommonFilters {

    public static class NonNull implements IMessageFilter<Object> {
        @Override
        public boolean accepts(Object message, SubscriptionContext context) {
            return message != null;
        }
    }

    public static class NonEmptyString implements IMessageFilter<String> {
        @Override
        public boolean accepts(String message, SubscriptionContext context) {
            return message != null && !message.isEmpty();
        }
    }

    public static class PositiveNumber implements IMessageFilter<Number> {
        @Override
        public boolean accepts(Number message, SubscriptionContext context) {
            return message != null && message.doubleValue() > 0;
        }
    }
}
```

### 3. Use Type Parameters for Type Safety

Always specify the type parameter in `IMessageFilter<T>` to get compile-time type checking:

```java
// Good - type safe
static class StringLengthFilter implements IMessageFilter<String> {
    @Override
    public boolean accepts(String message, SubscriptionContext context) {
        return message.length() > 10;  // IDE knows message is String
    }
}

// Avoid - loses type safety
static class StringLengthFilter implements IMessageFilter {  // Raw type
    @Override
    public boolean accepts(Object message, SubscriptionContext context) {
        return ((String) message).length() > 10;  // Requires cast
    }
}
```

### 4. Unit Test Your Filters

Filters are now easily testable as standalone classes:

```java
@Test
public void testLargeFileFilter() {
    LargeFileFilter filter = new LargeFileFilter();
    File largeFile = new File("/path/to/large/file");
    File smallFile = new File("/path/to/small/file");

    assertTrue(filter.accepts(largeFile, mockContext));
    assertFalse(filter.accepts(smallFile, mockContext));
}
```

## Common Pitfalls

### 1. Null Checks

EL expressions automatically handled nulls, but your filters must check explicitly:

```java
// EL automatically handled nulls
@Handler(condition = "msg.name == 'test'")

// Your filter must check for null
static class NameFilter implements IMessageFilter<Message> {
    @Override
    public boolean accepts(Message message, SubscriptionContext context) {
        return message != null &&
               message.getName() != null &&
               "test".equals(message.getName());
    }
}
```

### 2. String Comparisons

Always use `.equals()` instead of `==` for string comparisons:

```java
// Wrong - uses identity comparison
return message.getType() == "TEST";

// Correct - uses value comparison
return "TEST".equals(message.getType());
```

### 3. Generic Type Erasure

Be aware of type erasure when working with generics in filters:

```java
// This filter works with any List, not just List<String>
static class ListFilter implements IMessageFilter<List<String>> {
    @Override
    public boolean accepts(List<String> message, SubscriptionContext context) {
        // At runtime, you can't verify the list contains Strings
        return !message.isEmpty();
    }
}
```

## Troubleshooting

### Error: "cannot find symbol: class ElFilter"

**Cause:** Your code still references the removed `ElFilter` class.

**Solution:** Search for any remaining references to `ElFilter` and remove them.

### Error: Handler with `condition` parameter fails to compile

**Cause:** The `condition` parameter has been removed from the `@Handler` annotation.

**Solution:** Convert the condition to a filter class as shown in the examples above.

### Tests failing after migration

**Cause:** Tests may have been relying on EL expression evaluation behavior.

**Solution:** Update tests to use the new filter classes. See `ConditionalHandlerTest.java` for examples.

## Additional Resources

- [IMessageFilter JavaDoc](src/main/java/net/engio/mbassy/listener/IMessageFilter.java)
- [Filter Examples](examples/ListenerDefinition.java)
- [Filter Tests](src/test/java/net/engio/mbassy/FilterTest.java)
- [Conditional Handler Tests](src/test/java/net/engio/mbassy/ConditionalHandlerTest.java)

## Need Help?

If you encounter issues during migration:

1. Check the examples in `examples/ListenerDefinition.java`
2. Look at test cases in `src/test/java/net/engio/mbassy/ConditionalHandlerTest.java`
3. Open an issue on [GitHub](https://github.com/bennidi/mbassador/issues)

## Version History

- **v1.3.3** - Removed EL support, introduced lambda-compatible filters
- **v1.3.1** - Added support for meta-annotations (filter reuse)
- **v1.2.0** - Introduced EL expression support (now removed)
