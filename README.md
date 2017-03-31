<img src="https://travis-ci.org/bennidi/mbassador.svg?branch=master" alt="build-status" />

<a href="https://maven-badges.herokuapp.com/maven-central/net.engio/mbassador" target="blank" ><img src="https://img.shields.io/maven-central/v/net.engio/mbassador.svg" /></a>

<a href="http://bennidi.github.io/mbassador/" target="blank"><img src="assets/java.png?raw=true" alt="javadoc" width="20" height="20"/>javadoc</a>

<a href="wiki" target="blank"><img src="assets/wiki.png?raw=true" alt="wiki" width="50" height="20"/></a>


MBassador
=========

MBassador is a light-weight, high-performance event bus implementing the [publish subscribe pattern](https://en.wikipedia.org/wiki/Publish-subscribe_pattern). It is designed for ease of use and aims to be feature rich and extensible while preserving resource efficiency and performance. 

The core of MBassador's high performance is a **specialized data structure** that provides **non-blocking readers** and minimizes lock contention for writers such that performance degradation of concurrent read/write access is minimal. The advantages of this design are illustrated in this [github repository](https://github.com/bennidi/eventbus-performance).

The code is **production ready**: 86% instruction coverage, 82% branch coverage with highly randomized and concurrently run test sets, no severe bugs have been reported in the last 18 month. No modifications to the core will be made without thoroughly testing the code.


[Usage](#usage) | [Features](#features) | [Installation](#installation) | [Wiki](#wiki) | [Release Notes](#./changelog) | [Integrations](#integrations) | [Credits](#credits) | [Contribute](#contribute) | [License](#license)

<h2>Usage</h2>

Using MBassador in your project is very easy. Create as many instances of MBassador as you like (usually a singleton will do) ` bus = new MBassador()`, mark and configure your message handlers with `@Handler` annotations and finally register the listeners at any MBassador instance `bus.subscribe(aListener)`. Start sending messages to your listeners using one of MBassador's publication methods `bus.post(message).now()` or `bus.post(message).asynchronously()`.

As a first reference, consider this illustrative example. You might want to have a look at the collection of [examples](./examples) to see its features on more detail.

```java
      
// Define your handlers

@Listener(references = References.Strong)
class SimpleFileListener{

    @Handler
    public void handle(File file){
      // do something with the file
    }
    
    @Handler(delivery = Invoke.Asynchronously)
    public void expensiveOperation(File file){
      // do something with the file
    }
    
    @Handler(condition = "msg.size >= 10000")
    @Enveloped(messages = {HashMap.class, LinkedList.class})
    public void handleLarge(MessageEnvelope envelope) {
       // handle objects without common super type
    }

}

// somewhere else in your code

MBassador bus = new MBassador();
bus.subscribe (new SimpleFileListener());
bus.post(new File("/tmp/smallfile.csv")).now();
bus.post(new File("/tmp/bigfile.csv")).asynchronously();

```   

## Features


> Annotation driven

|Annotation|Function|
|:-----|:-----|
|`@Handler`|Mark a method as message handler|
|`@Listener`|Can be used to customize listener wide configuration like the used reference type|
|`@Enveloped`|A message envelope can be used to pass messages of different types into a single handler|
|`@Filter`|Add filtering to prevent certain messages from being published|

> Delivers everything, respects type hierarchy

Messages do not need to implement any interface and can be of any type. The class hierarchy of a message is considered during message delivery, such that handlers will also receive subtypes of the message type they consume for - e.g. a handler of Object.class receives everything. Messages that do not match any handler result in the publication of a `DeadMessage` object which wraps the original message. DeadMessage events can be handled by registering listeners that handle DeadMessage.

> Synchronous and asynchronous message delivery

There are two types of (a-)synchronicity when using MBassador: message dispatch and handler invocation. 
For message dispatch _synchronous_ means that the publishing method blocks until messages are delivered to all handlers and _asynchronous_ means that the publish method returns immediately and the message will be dispatched in another thread (fire and forget).

For handler invocation synchronous means that within a running publication all handlers are called sequentially. _Asynchronous_ means that the handler invocation is pushed into a queue and the next handler is invoked with waiting for the previous to finish.

> Configurable reference types

By default, MBassador uses **weak references** for listeners to relieve the programmer of the need to explicitly unsubscribe listeners that are not used anymore and **avoid memory-leaks**. This is very comfortable in container managed environments where listeners are created and destroyed by frameworks, i.e. Spring, Guice etc. Just add everything to the bus, it will ignore objects without handlers and automatically clean-up orphaned weak references after the garbage collector has done its job. 

Instead of using weak references, a listener can be configured to be referenced using strong references using `@Listener(references=References.Strong)`. Strongly referenced listeners will stick around until explicitly unsubscribed.

> Message filtering

MBassador offers static message filtering. Filters are configured using annotations and multiple filters can be attached to a single message handler. Since version 1.2.0 Java EL expressions in `@Handler` are another way to define conditional message dispatch. Messages that have matching handlers but do not pass the configured filters result in the publication of a FilteredMessage object which wraps the original message. FilteredMessage events can be handled by registering listeners that handle FilteredMessage.

> Enveloped messages

Message handlers can declare to receive an enveloped message using `Enveloped`. The envelope can wrap different types of messages to allow a single handler to handle multiple, unrelated message types.

> Handler priorities

A handler can be associated with a priority to influence the order in which messages are delivered when multiple matching handlers exist

> Custom error handling

Errors during message delivery are sent to all registered error handlers which can be added to the bus as necessary.

> Extensibility

MBassador is designed to be extensible with custom implementations of various components like message dispatchers and handler invocations (using the decorator pattern), metadata reader (you can add your own annotations) and factories for different kinds of objects. A configuration object is used to customize the different configurable parts, see [Features](https://github.com/bennidi/mbassador/wiki/Components#Feature)

## Installation
MBassador is available from the Maven Central Repository using the following coordinates:

```xml

<dependency>
    <groupId>net.engio</groupId>
    <artifactId>mbassador</artifactId>
    <version>{see.git.tags.for.latest.version}</version>
</dependency>

```

You can also download binary release and javadoc from the [maven central repository](http://search.maven.org/#search|ga|1|mbassador). Of course you can always clone the repository and build from source.

## Documentation
There is ongoing effort to extend documentation and provide code samples and detailed explanations of how the message bus works. Code samples can also be found in the various test cases. Please read about the terminology used in this project to avoid confusion and misunderstanding.

+ [javadoc](http://bennidi.github.io/mbassador/)
+ [wiki](wiki)
+ API examples on programcreek: [Handler](http://www.programcreek.com/java-api-examples/index.php?api=net.engio.mbassy.listener.Handler), [BusConfiguration](http://www.programcreek.com/java-api-examples/index.php?api=net.engio.mbassy.bus.config.BusConfiguration), [MBassador](http://www.programcreek.com/java-api-examples/index.php?api=net.engio.mbassy.bus.MBassador)


## Integrations

There is a [spring-extension](https://github.com/bennidi/mbassador-spring) available to support CDI-like transactional message sending in a Spring environment. This is a good example of integration with other frameworks. An example of [Guice integration](https://github.com/bennidi/mbassador/wiki/Guice-Integration) also exists.


## Credits
The initial inspiration for creating this component comes from Google Guava's event bus implementation.
I liked the simplicity of its design and I trust in the code quality of google libraries. The main reason it proved to be unusable for our scenario was that it uses strong references to the listeners.

Thanks to all [contributors](https://github.com/bennidi/mbassador/pulls?q=is%3Apr+is%3Aclosed), especially
+ [arne-vandamme](http://github.com/arne-vandamme) for adding support for [meta-annotations](https://github.com/bennidi/mbassador/pull/74)
+ [Bernd Rosstauscher](http://github.com/Rossi1337) for providing an initial integration with JUEL
+ [David Sowerby](http://github.com/davidsowerby) for answering user questions, his tutorial on [guice integration](bennidi/mbassador/wiki/guice-integration) and his various PRs
+ [dorkbox](http://github.com/dorkbox) for various PRs and his [work on performance tuning](http://github.com/bennidi/eventbus-performance/issues/1) which is still to be integrated
+ [durron597](http://github.com/durron597) for his many PRs and the help he offered to other users

Many thanks also to ej-technologies for providing an open source license of 
[![JProfiler](http://www.ej-technologies.com/images/banners/jprofiler_small.png)](http://www.ej-technologies.com/products/jprofiler/overview.html) and Jetbrains for a license of [IntelliJ IDEA](http://www.jetbrains.com/idea/)

OSS used by MBassador: [jUnit](http://www.junit.org) | [maven](http://www.maven.org) | [mockito](http://www.mockito.org) | [slf4j](http://www.slf4j.org) | [Odysseus JUEL](http://juel.sourceforge.net/guide/start.html)


##Contribute

Pick an issue from the list of open issues and start implementing. Make your PRs small and provide test code! Take a look at [this issue](bennidi/mbassador#109) for a good example.

> Note: Due to the complexity of the data structure and synchronization code it took quite a while to get a stable core. New features will only be implemented if they do not require significant modification to the core. The primary focus of MBassador is to provide high-performance extended pub/sub.

Sample code and documentation are both very appreciated contributions. Especially integration with different frameworks is of great value. Feel free and welcome to create Wiki pages to share your code and ideas. Example: [Guice integration](https://github.com/bennidi/mbassador/wiki/Guice-Integration)

<h2>License</h2>

This project is distributed under the terms of the MIT License. See file "LICENSE" for further reference.
