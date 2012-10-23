Mbassador
=========

Mbassador is a very light-weight message bus implementation following the publish subscribe pattern. It is designed
for ease of use and aims to be resource efficient and very fast. It was inspired by google guava's event bus which lacked some
features like weak references and ended up being even faster but offering more features. At its core it offers the following:

+ Delivers everything: Messages must not implement any interface and can be of any type (-> message bus is typed using generics with upper
bound being Object.class)
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

