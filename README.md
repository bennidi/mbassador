Mbassador
=========

Mbassador is a very light-weight message bus implementation following the publish subscribe pattern. It is designed
for ease of use and aims to be resource efficient and very fast. It was inspired by google guava's event bus which lacked some
features like weak references at that time. At its core it offers the following:

+ Delivers everything: Messages must not implement any interface and can be of any type (-> message bus is typed using generics with upper
bound being Object.class)
+ Follows type hierarchy: Class hierarchy of messages are considered during message delivery. This means that listeners will also receive
subtypes of the message type they are listening for, e.g. a listener for Object.class receives everything.
+ Annotation driven: To define and customize a message handler simply mark it with @Listener annotation
+ Synchronous and asynchronous message delivery: A handler can be invoked to handle a message either synchronously or
asynchronously. This is configurable for each handler via annotations. Message publication itself supports synchronous (method
blocks until messages are delivered to all handlers) or asynchronous (fire and forget) dispatch
+ Weak references: Mbassador uses weak references to all listening objects to relieve the programmer of the burden to explicitly unregister
listeners that are not used anymore (of course it is also possible to explicitly unregister a listener if needed). This is very comfortable
in certain environments where objects are created by frameworks, i.e. spring, guice etc. Just stuff everything into the message bus, it will
ignore objects without message handlers and automatically clean-up orphaned weak references after the garbage collector has done its job.
+ Filtering: Mbassador offers static message filtering. Filters are configured using annotations and multiple filters can be attached to
a single message handler
+ Error handling: Errors during message delivery are sent to an error handler of which a custom implementation can easily be plugged-in.
+ Ease of Use: Using Mbassador in your project is very easy. Create as many instances of Mbassador as you like (usually a singleton will do),
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
		@Listener(mode = Listener.Dispatch.Asynchronous)
		public void handleSubTestEvent(SubTestEvent event) {
            // do something more expensive here
		}

		// this handler will receive events of type SubTestEvent
        // or any subtabe and that passes the given filter(s)
        @Listener({@Filter(SpecialEventsOnly.class),@Filter(AnotherFilter.class)})
        public void handleFiltered(SubTestEvent event) {
           //do something special here
        }

Creation of message bus and registration of listeners:

        // create as many instances as necessary
        // bind it to any upper bound
        MBassador<TestEvent> bus = new MBassador<TestEvent();
        ListeningBean listener = new ListeningBean();
        // the listener will be registered using a weak-reference
        bus.subscribe(listener);
        // objects without handlers will be ignored
        bus.subscribe(new ClassWithoutAnyDefinedHandlers());


Message puclication:

        TestEvent event = new TestEvent();
        TestEvent subEvent = new SubTestEvent();

        bus.publishAsync(event); //returns immediately, publication will continue asynchronously
        bus.publish(subEvent);   // will return after each handler has been invoked


<h2>Planned features</h2>

+ Maven dependency: Add Mbassador to your project using maven. Coming soon!
+ Message handler priority: Message handlers can specify priority to influence order of message delivery




