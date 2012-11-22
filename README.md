Mbassador
=========

Mbassador is a very light-weight message (event) bus implementation following the publish subscribe pattern. It is designed
for ease of use and aims to be feature rich, extensible while preserving resource efficiency and performance.

It uses a specialized data structure to allow high throughput for concurrent access.

Read this documentation to get an overview of its features and how cool this message (event) bus actually is.
You can also check out the <a href="http://codeblock.engio.net/?p=37" target="_blank">performance comparison</a>
which also contains a partial list of the features of the compared implementations.

The current version is 1.0.1.RC

Table of contents:
+ [Features](#features)
+ [Usage](#usage)
+ [Installation](#installation)
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

        // this handler will be invoked asynchronously
		@Listener(dispatch = Mode.Asynchronous)
		public void handleSubTestEvent(SubTestEvent event) {
            // do something more expensive here
		}

		// this handler will receive events of type SubTestEvent
        // or any subtabe and that passes the given filter(s)
        @Listener(priority = 10,
                  dispatch = Mode.Synchronous,
                  filters = {@Filter(MessageFilter.None.class),@Filter(MessageFilter.All.class)})
        public void handleFiltered(SubTestEvent event) {
           //do something special here
        }

Creation of message bus and registration of listeners:

        // create as many instances as necessary
        // bind it to any upper bound
        MBassador<TestEvent> bus = new MBassador<TestEvent>();
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
This project contains a maven repository that will allow you to import MBassador as a dependency into your maven project.
Currently this is all that will be provided because publishing to a central repository requires extra project setup that
will be done as soon as enough people use this component. Until then, the following steps are necessary:

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
            &lt;groupId&gt;org.mbassy&lt;/groupId&gt;
            &lt;artifactId&gt;mbassador&lt;/artifactId&gt;
            &lt;version&gt;1.0.0.RC&lt;/version&gt;
        &lt;/dependency&gt;
    </pre></code>
 3. Run mvn clean package to have maven download and install the required version into your local repository

Of course you can always clone the repository and build from source

<h2>Roadmap</h2>
+ Checkout MBassador from one of the official maven repositories (as soon as the user base is big enough)
+ Spring integration with support for conditional message dispatch in transactional context (dispatch only after
successful commit etc.). Currently in beta, see <a href="https://github.com/bennidi/mbassador-spring">this</a> repository
+ MessageEnvelope for each dispatch that is passed to the handler and can be used for communication between handlers
during the running dispatch


<h2>Credits</h2>
The initial inspiration for creating this component came from looking at Google Guava's event bus implementation. Since
it did not provide all the features we needed in our project, I decided to create my own implementation. When I saw that
it outperformed the Guava implementation by far, I decided to share it with the community to see if others consider it worth
a shot.

<h2>Contribute</h2>

Any feature requests and feedback are more than welcome. You may suggest improvements either by submitting an
issue or by forking the repo and creating a pull request. I will try to respond as quickly as possible.

<h2>License</h2>

This project is distributed under the terms of the MIT License. See file "LICENSE" for further reference.






