MBassador
=========

MBassador is a very light-weight message (event) bus implementation following the publish subscribe pattern. It is designed for ease of use and aims to be feature rich and extensible while preserving resource efficiency and performance. The core of MBassador's high performance is a specialized data structure that minimizes lock contention such that performance degradation of concurrent access is minimal. The performance win of this design is illustrated in <a href="http://codeblock.engio.net/?p=37" target="_blank">performance comparison</a> and more recently in the [eventbus-performance](https://github.com/bennidi/eventbus-performance) github repository.

Using MBassador in your project is very easy. Create as many instances of MBassador as you like (usually a singleton will do) ` bus = new MBassador()`, mark and configure your message handlers with `@Handler` annotations and finally register the listeners at any MBassador instance `bus.subscribe(aListener)`. Start sending messages to your listeners using one of MBassador's publication methods `bus.post(message).now()` or `bus.post(message).asynchronously()`. Done!

Read this documentation to get an overview of MBassadors features. There is also some documentation in the Wiki - although admittedly not enough to make a developer happy (work is in progress). Additionally, you can browse the [javadoc](http://bennidi.github.io/mbassador/)

There is a [spring-extension](https://github.com/bennidi/mbassador-spring) available to support CDI-like transactional message sending in a Spring environment. It's beta but stable enough to give it a try.

Table of contents:
+ [Features](#features)
+ [Usage](#usage)
+ [Installation](#installation)
+ [Wiki](#wiki)
+ [Release Notes](#release-notes)
+ [Roadmap](#roadmap)
+ [Integrations](#integrations)
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
             .addPublicationErrorHandler(new IPublicationErrorHandler{...})
             .setProperty(Properties.Common.Id, "global bus")); // this is used for identification in #toString
        
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
        <version>1.2.1</version>
    </dependency>
```

You can also download binary release and javadoc from the [maven central repository](http://search.maven.org/#search|ga|1|mbassador). Of course you can always clone the repository and build from source.

<h2>Wiki</h2>
There is ongoing effort to extend documentation and provide code samples and detailed explanations of how the message bus works. Code samples can also be found in the various test cases. Please read about the terminology used in this project to avoid confusion and misunderstanding.

<h2>Release Notes</h2>

Release notes moved to the [changelog](./changelog).

##Integrations



<h2>Roadmap</h2>
There is no roadmap planning going on that deserves the name. There is a collection of useful features though. Check the issues labeled with [enhancement](https://github.com/bennidi/mbassador/labels/enhancement) or the available milestones. Comment if you would like to see the feature in a future release and/or want to share your ideas on the feature (or a variation thereof).

Please understand that I have limited time to include new features and that I will focus on stability and cleaner APIs. Adding features only works with well designed and thoroughly tested components. This is especially true for multi-threaded code and I am still not 100 percent convinced by the existing test design and some parts of the internal code layout.

Planned for release: [Spring integration](bennidi/mbassador-spring) (currently in beta state) with support for conditional message dispatch in transactional context (dispatch only after successful transaction commit etc.).


<h2>Credits</h2>
The initial inspiration for creating this component comes from Google Guava's event bus implementation.
I liked the simplicity of its design and I trust in the code quality of google libraries. The main reason it proved to be unusable for our scenario was that it uses strong references to the listeners.

I want to thank the development team from [friendsurance](www.friendsurance.de) for their support and feedback on the bus implementation and the management for allowing me to publish the component as an open source project.

I also want to thank all of the github users who have made little or larger [contributions](https://github.com/bennidi/mbassador/pulls?q=is%3Apr+is%3Aclosed). Thank you boys and girls, it is awesome to see
the open source idea working.
Special thanks go to
+ [arne-vandamme](http://github.com/arne-vandamme) for adding support for [meta-annotations](https://github.com/bennidi/mbassador/pull/74)
 + [Bernd Rosstauscher](http://github.com/Rossi1337) for providing an initial integration with JUEL
 + [David Sowerby](http://github.com/davidsowerby) for answering user questions, for his tutorial on [guice integration](bennidi/mbassador/wiki/guice-integration) and his various PRs
 + [dorkbox](http://github.com/dorkbox) for various PRs and his incredible [work on performance tuning](http://github.com/bennidi/eventbus-performance/issues/1) which is still to be integrated
 + [durron597](http://github.com/durron597) for his many PRs and the help he offered by answering user questions

Many thanks also to ej-technologies for providing me with an open source license of 
[![JProfiler](http://www.ej-technologies.com/images/banners/jprofiler_small.png)](http://www.ej-technologies.com/products/jprofiler/overview.html) 
and Jetbrains for a license of [IntelliJ IDEA](http://www.jetbrains.com/idea/)

And all the other open source projects that make this kind of development possible:

* [jUnit](http://www.junit.org)
* [maven](http://www.maven.org)
* [mockito](http://www.mockito.org)
* [slf4j](http://www.slf4j.org)
* [Odysseus JUEL](http://juel.sourceforge.net/guide/start.html)


Special thanks also to [Sonatype](http://www.sonatype.com/) for the hosting of their [oss nexus repository](https://oss.sonatype.org/).


<h2>Contribute</h2>

Any feature requests and feedback are more than welcome. You may suggest improvements or report bugs either by submitting an issue - I will try to respond as quickly as possible. Please try to be precise in the description of your requirements. Following a hands-on mentality please feel invited to contribute by by forking the repo and creating a pull request to submit the code you would like to be included. Make your PRs small and provide test code! Take a look at [this issue](bennidi/mbassador#109) for a good example.

Sample code and documentation are both very appreciated contributions. Especially integration with different frameworks is of great value. Feel free and welcome to create Wiki pages to share your code and ideas. Example: [Guice integration](https://github.com/bennidi/mbassador/wiki/Guice-Integration)

<h2>License</h2>

This project is distributed under the terms of the MIT License. See file "LICENSE" for further reference.






