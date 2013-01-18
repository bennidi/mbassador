MBassador
=========

MBassador is a very light-weight message (event) bus implementation following the publish subscribe pattern. It is designed
for ease of use and aims to be feature rich and extensible while preserving resource efficiency and performance. It uses a specialized
data structure to allow high throughput for concurrent access.

Read this documentation to get an overview of its features and how cool this message (event) bus actually is.
You can also check out the <a href="http://codeblock.engio.net/?p=37" target="_blank">performance comparison</a>
which also contains a partial list of the features of the compared implementations.

The current version is 1.1.1 and it is available from the Maven Central Repository. See the release notes for more details.

Table of contents:
+ [Features](#features)
+ [Usage](#usage)
+ [Installation](#installation)
+ [Release Notes](#releasenotes)
+ [Roadmap](#roadmap)
+ [Credits](#credits)
+ [Contribute](#contribute)
+ [License](#license)


<h2 name="features">Features</h2>

At its core MBassador offers the following features:

+ <em><strong>Annotation driven</em></strong>: To define and customize a message handler simply mark it with @Listener annotation
+ <em><strong>Delivers everything</em></strong>: Messages must not implement any interface and can be of any type (-> message bus is typed using generics with upper
bound being Object.class). The class hierarchy of a message is considered during message delivery. This means that listeners will also receive
subtypes of the message type they are listening for, e.g. a listener for Object.class receives everything.
+ <em><strong>Synchronous and asynchronous message delivery</em></strong>: A handler can be invoked to handle a message either synchronously or
asynchronously. This is configurable for each handler via annotations. Message publication itself supports synchronous (method
blocks until messages are delivered to all handlers) or asynchronous (fire and forget) dispatch
+ <em><strong>Weak references</em></strong>: Mbassador uses weak references to all listening objects to relieve the programmer of the burden to explicitly unregister
listeners that are not used anymore (of course it is also possible to explicitly unregister a listener if needed). This is very comfortable
in certain environments where objects are created by frameworks, i.e. spring, guice etc. Just stuff everything into the message bus, it will
ignore objects without message handlers and automatically clean-up orphaned weak references after the garbage collector has done its job.
+ <em><strong>Filtering</em></strong>: Mbassador offers static message filtering. Filters are configured using annotations and multiple filters can be attached to
a single message handler
+ <em><strong>Message envelopes</em></strong>: Message handlers can declare to receive an enveloped message. The envelope can wrap around different
types of messages. This allows for a single handler to handle multiple message types
+ <em><strong>Handler priorities</em></strong>: A listener can be associated with a priority to influence the order of the message delivery
+ <em><strong>Error handling</em></strong>: Errors during message delivery are sent to an error handler of which a custom implementation can easily be plugged-in.
+ <em><strong>Ease of Use</em></strong>: Using Mbassador in your project is very easy. Create as many instances of Mbassador as you like (usually a singleton will do),
mark and configure your message handlers with @Listener annotations and finally register the listeners at any Mbassador instance. Start
sending messages to your listeners using one of Mbassador's publication methods (sync or async). Done!



<h2>Usage</h2>

Listener definition (in any bean):

        // every event of type TestEvent or any subtype will be delivered
        // to this handler
        @Listener
		public void handleTestEvent(TestEvent event) {
			// do something
		}

        // this handler will be invoked concurrently
		@Listener(delivery = Mode.Concurrent)
		public void handleSubTestEvent(SubTestEvent event) {
            // do something more expensive here
		}

		// this handler will receive messages of type SubTestEvent
        // or any of its sub types that passe the given filter(s)
        @Listener(priority = 10,
                  delivery = Mode.Sequential,
                  filters = {@Filter(Filters.SpecialEvent.class)})
        public void handleFiltered(SubTestEvent event) {
           //do something special here
        }

        @Listener(delivery = Mode.Sequential, rejectSubtypes = true)
        @Enveloped(messages = {TestEvent.class, TestEvent2.class})
        public void handleVariousEvents(MessageEnvelope envelope) {
            // the envelope will contain either an instance of TestEvent or TestEvent2
            // if rejectSubtypes were set to 'false' (default) also subtypes of TestEvent or TestEvent2 would be allowed
        }


Creation of message bus and registration of listeners:

        // create as many instances as necessary
        // bind it to any upper bound
        MBassador<TestEvent> bus = new MBassador<TestEvent>(BusConfiguration.Default());
        ListeningBean listener = new ListeningBean();
        // the listener will be registered using a weak-reference
        bus.subscribe(listener);
        // objects without handlers will be ignored
        bus.subscribe(new ClassWithoutAnyDefinedHandlers());


Message publication:

        TestEvent event = new TestEvent();
        TestEvent subEvent = new SubTestEvent();

        bus.publishAsync(event); //returns immediately, publication will continue asynchronously
        bus.post(event).asynchronously(); // same as above
        bus.publish(subEvent);   // will return after each handler has been invoked
        bus.post(subEvent).now(); // same as above

<h2>Installation</h2>
Beginning with version 1.1.0 MBassador is available from the Maven Central Repository (Hooray!). Older versions are
still available from the included maven repository in this github repo but will be deleted in the future.
The preferred way of using MBassador is to simply add the dependency as shown in step two. Step one is only necessary
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

Of course you can always clone the repository and build from source

<h2>Release Notes</h2>

<h3>1.1.1</h3>

 + Introduced new property to @Listener annotation that allows to activate/deactivate any message handler
 + Full support of proxies created by cglib
 + Message handler inheritance changed! See wiki page about handler definition for more details.
 + Changed @Listener property "dispatch" to "delivery" and renamed the associated enumeration values to
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
 + Added handler configuration property to @Listener annotation to move from message filtering to more specific implementation
 of this feature

<h3>1.0.4.RC</h3>

  + Introduced BusConfiguration as a central class to encapsulate configurational aspects


<h2>Roadmap</h2>
+ Spring integration with support for conditional message dispatch in transactional context (dispatch only after
successful commit etc.). Currently in beta, see <a href="https://github.com/bennidi/mbassador-spring">this</a> repository


<h2>Credits</h2>
The initial inspiration for creating this component came from looking at Google Guava's event bus implementation. Since
it did not provide all the features we needed in our project, I decided to create my own implementation. It matured to be
quite a feature rich and yet very efficient and performant implementation.

I want to thank the development team from friendsurance (www.friendsurance.de) for their support and feedback on the bus
implementation and the management of friendsurance for allowing me to publish the component as an open source project.

<h2>Contribute</h2>

Any feature requests and feedback are more than welcome. You may suggest improvements either by submitting an
issue or by forking the repo and creating a pull request. I will try to respond as quickly as possible.

<h2>License</h2>

This project is distributed under the terms of the MIT License. See file "LICENSE" for further reference.






