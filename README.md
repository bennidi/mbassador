MBassador
=========

MBassador is a very light-weight message (event) bus implementation following the publish subscribe pattern. It is designed for ease of use and aims to be feature rich and extensible while preserving resource efficiency and performance. The core of MBassador's high performance is a specialized data structure that minimizes lock contention such that performance degradation of concurrent access is minimal. The performance win of this design is illustrated in <a href="http://codeblock.engio.net/?p=37" target="_blank">performance comparison</a> and more recently in the [eventbus-performance](https://github.com/bennidi/eventbus-performance) github repository.

Using MBassador in your project is very easy. Create as many instances of MBassador as you like (usually a singleton will do) ` bus = new MBassador(BusConfiguration.SyncAsync())`, mark and configure your message handlers with `@Handler` annotations and finally register the listeners at any MBassador instance `bus.subscribe(aListener)`. Start sending messages to your listeners using one of MBassador's publication methods `bus.post(message).now()` or `bus.post(message).asynchronously()`. Done!

Read this documentation to get an overview of MBassadors features. There is also some documentation in the Wiki - although admittedly
not enough to make a developer happy (work is in progress). Additionally, you can browse the [javadoc](http://bennidi.github.io/mbassador/)

There is a [spring-extension](https://github.com/bennidi/mbassador-spring) available to support CDI-like transactional message sending in a Spring environment. It's beta but stable enough to give it a try.

Table of contents:
+ [Features](#features)
+ [Usage](#usage)
+ [Installation](#installation)
+ [Wiki](#wiki)
+ [Release Notes](#releasenotes)
+ [Roadmap](#roadmap)
+ [Credits](#credits)
+ [Contribute](#contribute)
+ [License](#license)


<h2 name="features">Features</h2>


> Annotation driven

|Annotation|Function|
|:-----|:-----|
|`@Handler`|Defines and customizes a message handler. Any well-formed method annotated with `@Handler` will cause instances of the defining class to be treated as message listeners|
|`@Listener`|Can be used to customize listener wide configuration like the used reference type|
|`@Enveloped`|A message envelope can be used to pass messages of different types into a single handler|
|`@Filter`|Add filtering to prevent certain messages from being published|

> Delivers everything

Messages do not need to implement any interface and can be of any type. It is possible though to define an upper bound of the message type using generics. The class hierarchy of a message is considered during message delivery, such that handlers will also receive subtypes of the message type they consume for - e.g. a handler of Object.class receives everything. Messages that do not match any handler result in the publication of a `DeadMessage` object which wraps the original message. DeadMessage events can be handled by registering listeners that handle DeadMessage.

> Synchronous and asynchronous message delivery

A handler can be invoked to handle a message either synchronously or asynchronously. This is configurable for each handler via annotations. Message publication itself supports synchronous (method blocks until messages are delivered to all handlers) or asynchronous (fire and forget) dispatch

> Configurable reference types

By default, MBassador uses weak references for listeners to relieve the programmer of the need to explicitly unsubscribe listeners that are not used anymore and avoid memory-leaks. This is very comfortable in container managed environments where listeners are created and destroyed by frameworks, i.e. Spring, Guice etc. Just stuff everything into the message bus, it will ignore objects without message handlers and automatically clean-up orphaned weak references after the garbage collector has done its job. Instead of using weak references, a listener can be configured to be referenced using strong references using `@Listener(references=References.Strong)`. Strongly referenced listeners will stick around until explicitly unsubscribed.

> Filtering

MBassador offers static message filtering. Filters are configured using annotations and multiple filters can be attached to a single message handler. Since version 1.2.0 Java EL expressions in `@Handler` are another way to define conditional message dispatch. Messages that have matching handlers but do not pass the configured filters result in the publication of a FilteredMessage object which wraps the original message. FilteredMessage events can be handled by registering listeners that handle FilteredMessage.

> Enveloped messages

Message handlers can declare to receive an enveloped message using `Enveloped`. The envelope can wrap different types of messages to allow a single handler to handle multiple, unrelated message types.

> Handler priorities

A handler can be associated with a priority to influence the order in which messages are delivered when multiple matching handlers exist

> Custom error handling

Errors during message delivery are sent to all registered error handlers which can be added to the bus as necessary.

> Extensibility

MBassador is designed to be extensible with custom implementations of various components like message dispatchers and handler invocations (using the decorator pattern), metadata reader (you can add your own annotations) and factories for different kinds of objects. A configuration object is used to customize the different configurable parts, see [Features](https://github.com/bennidi/mbassador/wiki/Components#Feature)


<h2>Usage</h2>

### Handler definition

Message handlers are defined via annotations added to instance methods. The simplest definition is to just use `@Handler` without changing any parameters or adding of any other annotations.
> NOTE: MBassador uses WEAK REFERENCES by default. If you do not hold references to your listeners somewhere else they will be garbage collected! This can be changed by adding `@Listener(references=References.Strong)` to the defining class


        // every message of type TestMessage or any subtype will be delivered
        // to this handler
        @Handler
		public void handleTestMessage(TestMessage message) {
			// do something
		}

		// every message of type TestMessage or any subtype will be delivered
        // to this handler
        @Handler
        public void handleTestMessageStrong(TestMessage message) {
            // do something
        }

        // this handler will be invoked asynchronously (in a different thread)
		@Handler(delivery = Invoke.Asynchronously)
		public void handleSubTestMessage(SubTestMessage message) {
            // do something more expensive here
		}

		// this handler will receive messages of type SubTestMessage
        // or any of its sub types that passe the given filter(s)
        @Handler(priority = 10,
                  delivery = Invoke.Synchronously,
                  filters = {@Filter(Filters.SpecialMessage.class)})
        public void handleFiltered(SubTestMessage message) {
           //do something special here
        }

        @Handler(delivery = Invoke.Synchronously, rejectSubtypes = true)
        @Enveloped(messages = {TestMessage.class, TestMessage2.class})
        public void handleUnrelatedMessageTypes(MessageEnvelope envelope) {
            // the envelope will contain either an instance of TestMessage or TestMessage2
            // if rejectSubtypes were set to 'false' (default) also subtypes of TestMessage or TestMessage2 would be allowed
        }


        // configure a listener to be stored using strong instead of weak references
        @Listener(references = References.Strong)
        public class MessageListener{

            // any handler definitions

        }

        // conditional handler using Java EL expressions
        @Handler(condition = "msg.getType().equals('XYZ') && msg.getSize() == 1")
        public void handleMethodAccessEL(TestEvent message) {
            message.handledBy("handleMethodAccessEL");
        }


### Message bus creation

        // Use a default constructor for convenience and create as many instances as you like
        MBassador<TestMessage> bus = new MBassador<TestMessage>();
        MBassador<String> bus2 = new MBassador<String>();
       
        // Use feature driven configuration to have more control over the configuration details
        MBassador globalBus = new MBassador(new BusConfiguration()
             .addFeature(Feature.SyncPubSub.Default())
             .addFeature(Feature.AsynchronousHandlerInvocation.Default())
             .addFeature(Feature.AsynchronousMessageDispatch.Default())
             .setProperty(Properties.Common.Id, "global bus")
             .setProperty(Properties.Handler.PublicationError, new IPublicationErrorHandler{...}));
        
### Listener subscription
        ListeningBean listener = new ListeningBean();
        // the listener will be registered using a weak-reference if not configured otherwise with @Listener
        bus.subscribe(listener);
        // objects without handlers will be ignored
        bus.subscribe(new ClassWithoutAnyDefinedHandlers());


### Message publication

        TestMessage message = new TestMessage();
        TestMessage subMessage = new SubTestMessage();

Messages can be published asynchronously in another thread (fire and forget):

        bus.publishAsync(message); //returns immediately, publication will continue asynchronously
        bus.post(message).asynchronously(); // same as above
        
Message can be published synchronously in the same thread:        

        bus.publish(subMessage);   // will return after each handler has been invoked
        bus.post(subMessage).now(); // same as above

<h2>Installation</h2>
MBassador is available from the Maven Central Repository using the following coordinates:
```xml
    <dependency>
        <groupId>net.engio</groupId>
        <artifactId>mbassador</artifactId>
        <version>1.2.0</version>
    </dependency>
```

You can also download binary release and javadoc from the [maven central repository](http://search.maven.org/#search|ga|1|mbassador). Of course you can always clone the repository and build from source.

<h2>Wiki</h2>
There is ongoing effort to extend documentation and provide code samples and detailed explanations of how the message bus works. Code samples can also be found in the various test cases. Please read about the terminology used in this project to avoid confusion and misunderstanding.

<h2>Release Notes</h2>

### [1.2.1](milestones/1.2.1)
 + Not yet released!
 + Centralized handling of common (and arbitrary) properties (see BusConfiguration#setProperty and net.engio.mbassy.bus.common.Properties)
 + Each bus now has a configurable id and respective #toString() implementation (useful for debugging)
 + Each bus now has a default logger (System.out) for publication errors (exception in handlers) which can be replaced with BusConfiguration#setProperty 
 + __API-Changes:__
   + Interface `IMessageFilter` now receives the SubscriptionContext as second parameter. This gives access to the bus runtime within filter logic (useful for error propagation). -> Change your filters signature. You can access the `MessageHandler` object directly from the context. 
   + Removed deprecated method BusConfiguration.SyncAsync() -> Use default constructor or feature based configuration instead
   + Deleted interface ISyncMessageBus since it was merely an aggregation of existing interfaces -> Replace with GenericMessagePublicationSupport

### 1.2.0
 + Added support for conditional handlers using Java EL. Thanks to Bernd Rosstauscher for the initial implementation.
 + BREAKING CHANGES in BusConfiguration
   + Complete redesign of configuration setup using Features instead of simple get/set parameters. This will allow
 to flexibly combine features and still be able to exclude those not available in certain environments,for example, threading and reflection in GWT (this will be part of future releases)
   + Properties formerly located in BusConfiguration now moved to their respective Feature class
   + Removed all SyncXX related interfaces and config implementations. There is now only one `BusConfiguration`
 with its corresponding interface which will be used for all types of message bus implementations


### 1.1.10
 + Fixed broken sort order of prioritized handlers (see #58)
 + Addressed issue #63 by making the constructor of `MessageHandler` use a map of properties and by replacing dependencies to
  all MBassador specific annotations with Java primitives and simple interfaces
 + Small refactorings (moved stuff around to have cleaner packaging)
 + MessageBus.getExecutor() is now deprecated and will be removed with next release -> use the runtime to get access to it.
 + Introduced BusFactory with convenience methods for creating bus instances for different message dispatching scenarios like
 asynchronous FIFO (asynchronous message publications guaranteed to be delivered in the order they occurred)
 + Renamed runtime property of `BusRuntime` "handler.async-service" to "handler.async.executor"

### 1.1.9

 + Fixed memory leak reported in issue #53

### 1.1.8

 + Internal refactorings and code improvements
 + Fixed #44 #45 #47
 + NOTE: This release has a known issue with weak references which introduces a memory leak and is fixed in 1.1.9. The
 version 1.1.8 is not available from the central repository


### 1.1.7

 + Console Logger not added to message bus instances by default -> use addErrorHandler(IPublicationErrorHandler.ConsoleLogger)
 + Fixed race conditions in net.engio.mbassy.subscription.Subscription and of WeakConcurrentSet.contains()
 + Improved message hierarchy handling: Now interfaces, enums , (abstract) classes should work in all combinations
 + Prevented dispatcher threads from dying on exceptions
 + Improved test-infrastructure and increased test-coverage
 + Thanks for your feedback!

### 1.1.6

 + Added support for choosing between strong and weak references using the new @Listener annotation. @Listener can be
 added to any class that defines message handlers and allows to configure which reference type is used
 + Custom handler invocations: It is possible to provide a custom handler invocation for each message handler, see "invocation"
 property of @Handler
 + Changed packaging to "bundle" to support OSGI environments
 + Synchronization of message handlers via @Synchronized: Handlers that are not thread-safe can be synchronized to guarantee
  that only one thread at a time can invoke that handler
 + Created a message bus implementation that does not use threading to support use in non-multi-threaded environments like GWT,
 see ISyncMessageBus

### 1.1.3

 + Added support for FilteredMessage event
 + Renamed @Listener to @Handler and DeadEvent to DeadMessage to increase alignment with the established terminology.
 Sorry for the inconvenience since this will lead to compile errors but good old find&replace will do
 + Repackaging and refactoring of some parts
 + Introduced message publication factories as configurable components to make MBassador more extensible/customizable
 + Added more documentation and unit tests

### 1.1.1

 + Added support for DeadMessage event
 + Introduced new property to @Handler annotation that allows to activate/deactivate any message handler
 + Full support of proxies created by cglib
 + Message handler inheritance changed! See wiki page about handler definition for more details.
 + Changed @Handler property "dispatch" to "delivery" and renamed the associated enumeration values to
   more precisely indicate their meaning
 + Added more unit tests

### 1.1.0

First stable release!

 + Refactoring and repackaging
 + More exhaustive unit tests
 + Installation from the central repository

### 1.0.6.RC

 + Fixed behaviour with capacity bound blocking queue such that there now are two methods to schedule a message
 asynchronously. One will block until capacity becomes available, the other will timeout after a specified amount of
 time.
 +  Additional unit tests

### 1.0.5.RC

 + Added MessageEnvelope and @Enveloped annotation to configure handlers that might receive arbitrary message type
 + Added handler configuration property to @Handler annotation to move from message filtering to more specific implementation
 of this feature

### 1.0.4.RC

  + Introduced BusConfiguration as a central class to encapsulate configurational aspects


<h2>Roadmap</h2>
Check the issues labeled 'enhancement'. Comment if you would like to see the feature in a future release and/or want to share
your ideas on the feature (or a variation thereof).
Please understand that I have limited time to include new features and that I will focus on stability and cleaner APIs.
Adding features only works with well designed and thoroughly tested components. This is especially true for multi-threaded code
and I am still not 100 percent happy with the existing test design and some parts of the internal code layout.

Planned for release:Spring integration with support for conditional message dispatch in transactional context (dispatch only after
successful transaction commit etc.). Currently in beta, see <a href="https://github.com/bennidi/mbassador-spring">this</a> repository


<h2>Credits</h2>
The initial inspiration for creating this component came from trying out Google Guava's event bus implementation.
I liked the simplicity of its design and I do trust the developers at Google a lot, so I was happy to find that they also
provided an event bus system. The main reason it proved to be unusable for our scenario was that it uses strong references
to the listeners such that every object has to be explicitly unsubscribed. This was difficult in our Spring managed environment.
Finally, I decided to create a custom implementation, which then matured to be stable, extensible and yet very efficient.

I want to thank the development team from friendsurance (www.friendsurance.de) for their support and feedback on the bus implementation and the management of friendsurance for allowing me to publish the component as an open source project.

Many thanks also to ej-technologies for providing me with an open source license of [![JProfiler](http://www.ej-technologies.com/images/banners/jprofiler_small.png)](http://www.ej-technologies.com/products/jprofiler/overview.html) and Jetbrains for a license of [IntelliJ IDEA](http://www.jetbrains.com/idea/)

<h2>Contribute</h2>

Any feature requests and feedback are more than welcome. You may suggest improvements either by submitting an
issue or by forking the repo and creating a pull request. I will try to respond as quickly as possible.

Sample code and documentation are both very appreciated contributions. Especially integration with different frameworks
such as Spring, Guice or other is of great value. Feel free and welcome to create Wiki pages to share your code and ideas.

<h2>License</h2>

This project is distributed under the terms of the MIT License. See file "LICENSE" for further reference.






