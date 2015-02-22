MBassador understands a variety of message handler configurations that will affect how a message
is delivered to a specific listener. There are properties to control the handling of subclasses
of the specified message (the method parameter), the execution order of handlers for the same message type,
filters, delivery modes etc.

<h2>Message handler properties</h2>

<table>
  <tr> <td>Property</td> <td>Description</td> <td>Default</td> </tr>

  <tr>
        <td>delivery</td>
        <td>Message handler invocation can be configured to run
            <ul>
                <li>Synchronously: One handler at a time within a given message publication. Each invocation occurs from the same thread</li>
                <li>Asynchronously: Multiple threads are used within a given message publication. Each handler invocation
                runs in a separate thread.Note:The number of parallel threads is configurable per instance using the BusConfiguration</li>
            </ul>
            Note: Use @Synchronized if your handler does not allow multiple, concurrent message publications, i.e.
            handlers that are not thread-safe but are used in a multi-threaded environment where asynchronous message publication
            is possible.
        </td>
        <td>Synchronously</td>
  </tr>

  <tr>
        <td>priority</td>
        <td>The priority is used to determine the order in which a message is delivered to
            different message handlers that consume the same message type. Higher priority means
            higher precedence in message delivery.</td>
        <td>0</td>
  </tr>

  <tr>
      <td>rejectSubtypes</td>
      <td>The primary message type consumed by a message handler is determined by the type of
          its parameter.Polymorphism does allow any sub type of that message type to be delivered
          to the handler as well, which is the default behaviour of any message handler.
          The handler can be configured to not receiving any sub types by specifying thus using this
          property.
      </td>
      <td>false</td>
  </tr>

    <tr>
        <td>enabled</td>
        <td>A handler can be explicitly disabled to not take part in message delivery.
        </td>
        <td>true</td>
    </tr>

    <tr>
        <td>strongReferencess</td>
        <td>Whether the bus should use storng references to the listeners instead of weak references
        </td>
        <td>false</td>
        </tr>
    <tr>
        <td>invocation</td>
        <td>Specify a custom implementation for the handler invocation. By default, a generic implementation
        that uses reflection will be used. Note: A custom implementation will not be faster than the generic one
        since there are heavy optimizations by the JVM using JIT-Compiler and more.
        </td>
        <td>false</td>
    </tr>


</table>

<h2>Message handler definition</h2>

The standard message handler definition looks like the following.It will
receive all messages of type TestEvent or any subtype sequentially.

        // every message of type TestEvent or any subtype will be delivered
        // to this handler
        @Handler
		public void handleTestEvent(TestEvent event) {
			// do something
		}



This handler will receive all messages of type SubTestEvent or any subtype

        // handler invocation will occur in a different thread
		@Handler(delivery = Invoke.Asynchronously)
		public void handleSubTestEvent(SubTestEvent event) {
            // do something more expensive here
		}

This handler will receive all messages of type SubTestEvent or any subtype,
given that they pass the specified filters. This handler will be invoked before the formerly
defined one, since it specifies a higher priority

		// this handler will receive messages of type SubTestEvent
        // or any of its sub types that passe the given filter(s)
        @Handler(priority = 10,
                  dispatch = Invoke.Synchronously,
                  filters = {@Filter(Filters.SpecialEvent.class)})
        public void handleFiltered(SubTestEvent event) {
           //do something special here
        }


        @Handler(dispatch = Mode.Synchronous, rejectSubtypes = true)
        @Enveloped(messages = {TestEvent.class, TestEvent2.class})
        public void handleVariousEvents(MessageEnvelope envelope) {
            // the envelope will contain either an instance of TestEvent or TestEvent2
            // if rejectSubtypes were set to 'false' (default) also subtypes of TestEvent or TestEvent2 would be allowed
        }



<h2>Enveloped message handlers</h2>

Since one parameter (the message) does not offer a great deal of flexibility if different types
of messages should be consumed, there exists the possibility to wrap a message inside an envelope.
An enveloped message handler specifies the message type it consumes by using the @Enveloped annotation
in addition to the @Handler annotation. All configurations of @Handler apply to each of the specified
message types.

        @Handler(dispatch = Mode.Synchronous, rejectSubtypes = true)
        @Enveloped(messages = {TestEvent.class, TestEvent2.class})
        public void handleVariousEvents(MessageEnvelope envelope) {
            // the envelope will contain either an instance of TestEvent or TestEvent2
            // if rejectSubtypes were set to 'false' (default) also subtypes of TestEvent or TestEvent2 would be allowed
        }


<h2>Inheritance</h2>

Message handler inheritance corresponds to inheritance of methods as defined in the Java language itself.
A subclass of any class that defines message handlers will inherit these handler and their configuration.
It is possible to change (override) the configuration simply by overriding the super class' method and
specifying a different configuration. This way, it is also possible to deactivate a message handler of
a super class by setting the "enabled" property to "false" on the overridden method.
If a class overrides a method that is already configured as a message handler
it is still considered a message handler but of course the implementation of the overriding class
will be used.



