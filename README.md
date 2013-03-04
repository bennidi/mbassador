MBassador
=========

MBassador is a very light-weight message (event) bus implementation following the publish subscribe pattern. It is designed
for ease of use and aims to be feature rich and extensible while preserving resource efficiency and performance. It uses a specialized
data structure to allow high throughput for concurrent access.

Read this documentation to get an overview of its features. You can also check out the <a href="http://codeblock.engio.net/?p=37" target="_blank">performance comparison</a>
which also contains a partial list of the features of the compared implementations.

The current version is 1.1.1 and it is available from the Maven Central Repository. See the release notes for more details.

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

At its core MBassador offers the following features:

+ <em><strong>Annotation driven</em></strong>: To define and customize a message handler simply mark it with @Handler annotation
+ <em><strong>Delivers everything</em></strong>: Messages must not implement any interface and can be of any type. It is
possible though to define an upper bound of the message type using generics. The class hierarchy of a message is considered during message delivery.
This means that listeners will also receivesubtypes of the message type they are listening for, e.g. a listener for Object.class receives everything.
+ <em><strong>Synchronous and asynchronous message delivery</em></strong>: A handler can be invoked to handle a message either synchronously or
asynchronously. This is configurable for each handler via annotations. Message publication itself supports synchronous (method
blocks until messages are delivered to all handlers) or asynchronous (fire and forget) dispatch
+ <em><strong>Weak references</em></strong>: MBassador uses weak references to all listening objects to relieve the programmer of the burden to explicitly unregister
listeners that are not used anymore (of course it is also possible to explicitly unregister a listener if needed). This is very comfortable
in certain environments where listeners are managed by frameworks, i.e. spring, guice etc. Just stuff everything into the message bus, it will
ignore objects without message handlers and automatically clean-up orphaned weak references after the garbage collector has done its job.
+ <em><strong>Filtering</em></strong>: MBassador offers static message filtering. Filters are configured using annotations and multiple filters can be attached to
a single message handler
+ <em><strong>Message envelopes</em></strong>: Message handlers can declare to receive an enveloped message. The envelope can wrap different
types of messages. This allows for a single handler to handle multiple (unrelated) message types.
+ <em><strong>Handler priorities</em></strong>: A listener can be associated with a priority to influence the order in which messages are delivered when multiple handlers exist
+ <em><strong>Custom error handling</em></strong>: Errors during message delivery are sent to all registered error handlers which can be added to the bus as necessary.
+ <em><strong>Dead Event</em></strong>: Messages that do not match any handler result in the publication of a DeadEvent object which wraps the original message. Dead events
can be handled by registering listeners that handle DeadEvent.
+ <em><strong>Dead Event</em></strong>: Messages that have matching handlers but do not pass the configured filters result in the publication of a FilteredEvent object which wraps the original message.
Filtered events can be handled by registering listeners that handle FilteredEvent.
+ <em><strong>Extensibility</em></strong>:MBassador is designed to be extensible with custom implementations of various components like message
dispatchers and handler invocations (using the decorator pattern), metadata reader (you can add your own annotations) and factories for different
 kinds of object.A configuration object can be passed to on instance creation to specify the different configurable parts
+ <em><strong>Ease of Use</em></strong>: Using MBassador in your project is very easy. Create as many instances of MBassador as you like (usually a singleton will do),
mark and configure your message handlers with @Handler annotations and finally register the listeners at any MBassador instance. Start
sending messages to your listeners using one of MBassador's publication methods (sync or async). Done!



<h2>Usage</h2>

Listener definition (in any bean):

        // every event of type TestMessage or any subtype will be delivered
        // to this handler
        @Handler
		public void handleTestMessage(TestMessage event) {
			// do something
		}

        // this handler will be invoked concurrently
		@Handler(delivery = Mode.Concurrent)
		public void handleSubTestMessage(SubTestMessage event) {
            // do something more expensive here
		}

		// this handler will receive messages of type SubTestMessage
        // or any of its sub types that passe the given filter(s)
        @Handler(priority = 10,
                  delivery = Mode.Sequential,
                  filters = {@Filter(Filters.SpecialMessage.class)})
        public void handleFiltered(SubTestMessage event) {
           //do something special here
        }

        @Handler(delivery = Mode.Sequential, rejectSubtypes = true)
        @Enveloped(messages = {TestMessage.class, TestMessage2.class})
        public void handleVariousEvents(MessageEnvelope envelope) {
            // the envelope will contain either an instance of TestMessage or TestMessage2
            // if rejectSubtypes were set to 'false' (default) also subtypes of TestMessage or TestMessage2 would be allowed
        }


Creation of message bus and registration of listeners:

        // create as many instances as necessary
        // bind it to any upper bound
        MBassador<TestMessage> bus = new MBassador<TestMessage>(BusConfiguration.Default());
        ListeningBean listener = new ListeningBean();
        // the listener will be registered using a weak-reference
        bus.subscribe(listener);
        // objects without handlers will be ignored
        bus.subscribe(new ClassWithoutAnyDefinedHandlers());


Message publication:

        TestMessage message = new TestMessage();
        TestMessage subMessage = new SubTestMessage();

        bus.publishAsync(message); //returns immediately, publication will continue asynchronously
        bus.post(message).asynchronously(); // same as above
        bus.publish(subMessage);   // will return after each handler has been invoked
        bus.post(subMessage).now(); // same as above

<h2>Installation</h2>
Beginning with version 1.1.0 MBassador is available from the Maven Central Repository (Hooray!). Older versions are
still available from the included maven repository in this github repo but will be deleted in the future.
The recommended way of using MBassador in your project is to add the dependency as shown in step two. Step one is only necessary
if you want to use an older version that is not available in the central repository.

 1. Add the repository location to your pom.xml
    <pre><code class="xml">
    &lt;repositories&gt;
        &lt;repository&gt;
            &lt;id&gt;mbassador-github-repo&lt;/id&gt;
            &lt;url&gt;https://raw.github.com/bennidi/mbassador/master/maven &lt;/url&gt;
        &lt;/repository&gt;
    &lt;/repositories&gt;
    </pre></code>
 2. Add the MBassador dependency to your pom.xml. You can check which versions are available by browsing
    the git repository online.
    <pre><code class="xml">
        &lt;dependency&gt;
            &lt;groupId&gt;net.engio&lt;/groupId&gt;
            &lt;artifactId&gt;mbassador&lt;/artifactId&gt;
            &lt;version&gt;1.1.0&lt;/version&gt;
        &lt;/dependency&gt;
    </pre></code>
 3. Run mvn clean package to have maven download and install the required version into your local repository

Of course you can always clone the repository and build from source.

<h2>Wiki</h2>
There is ongoing afford to extend documentation and provide code samples and detailed explanations of how the message bus
works. Code samples can also be found in the various test cases. Please read about the terminology used in this project
to avoid confusion and misunderstanding.

<h2>Release Notes</h2>

<h3>1.1.3</h3>

 + Added support for FilteredMessage event
 + Renamed @Listener to @Handler and DeadEvent to DeadMessage to increase alignment with the established terminology.
 Sorry for the inconvenience since this will lead to compile errors but good old find&replace will do
 + Repackaging and refactoring of some parts
 + Introduced message publication factories as configurable components to make MBassador more extensible/customizable
 + Added more documentation and unit tests

<h3>1.1.1</h3>

 + Added support for DeadMessage event
 + Introduced new property to @Handler annotation that allows to activate/deactivate any message handler
 + Full support of proxies created by cglib
 + Message handler inheritance changed! See wiki page about handler definition for more details.
 + Changed @Handler property "dispatch" to "delivery" and renamed the associated enumeration values to
   more precisely indicate their meaning
 + Added more unit tests

<h3>1.1.0</h3>

First stable release!

 + Refactoring and repackaging
 + More exhaustive unit tests
 + Installation from the central repository

<h3>1.0.6.RC</h3>

 + Fixed behaviour with capacity bound blocking queue such that there now are two methods to schedule a message
 asynchronously. One will block until capacity becomes available, the other will timeout after a specified amount of
 time.
 +  Added unit tests

<h3>1.0.5.RC</h3>

 + Added MessageEnvelope and @Enveloped annotation to configure handlers that might receive arbitrary message type
 + Added handler configuration property to @Handler annotation to move from message filtering to more specific implementation
 of this feature

<h3>1.0.4.RC</h3>

  + Introduced BusConfiguration as a central class to encapsulate configurational aspects


<h2>Roadmap</h2>
+ Spring integration with support for conditional message dispatch in transactional context (dispatch only after
successful commit etc.). Currently in beta, see <a href="https://github.com/bennidi/mbassador-spring">this</a> repository


<h2>Credits</h2>
The initial inspiration for creating this component came from trying out Google Guava's event bus implementation.
I liked the simplicity of its design and I do trust the developers at Google a lot, so I was happy to find that they also
provided an event bus system. The main reason it proved to be unusable for our scenario was that it uses strong references
to the listeners such that every object has to be explicitly deregistered. This was difficult in our Spring managed environment.
Finally, I decided to create a custom implementation, which then matured to be stable, extensibel and yet very efficient
and well performing.

I want to thank the development team from friendsurance (www.friendsurance.de) for their support and feedback on the bus
implementation and the management of friendsurance for allowing me to publish the component as an open source project.

<h2>Contribute</h2>

Any feature requests and feedback are more than welcome. You may suggest improvements either by submitting an
issue or by forking the repo and creating a pull request. I will try to respond as quickly as possible.

Sample code and documentation are both very appreciated contributions. Especially integration with different frameworks
such as Spring, Guice or other is of great value. Feel free and welcome to create Wiki pages to share your code and ideas.

<h2>License</h2>

This project is distributed under the terms of the MIT License. See file "LICENSE" for further reference.






