Listener Definition
===================

MBassador allows a variety of message handler configurations that will affect how a message
is delivered to a specific listener. There are properties to control the handling of subclasses
of the specified message (the method parameter), the execution order of handlers for the same message,
filters, delivery modes etc.

<h2>Message handler properties</h2>

<table>
  <tr> <td>Property</td> <td>Meaning</td> <td>Default</td> </tr>

  <tr>
        <td>delivery</td>
        <td>Message delivery can either run sequentially(i.e. one listener at a time) or concurrently
            (i.e. multiple threads are used to deliver the same message to different listeners).
            Note:The number of parallel threads is configurable per instance using the BusConfiguration</td>
        <td>Sequential</td>
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


</table>

<h2>Message handler definition</h2>

The standard message handler definition looks like the following.It will
receive all messages of type TestEvent or any subtype sequentially.

        // every message of type TestEvent or any subtype will be delivered
        // to this handler
        @Listener
		public void handleTestEvent(TestEvent event) {
			// do something
		}



This handler will receive all messages of type SubTestEvent or any subtype concurrently

        // this handler will be invoked concurrently
		@Listener(delivery = Mode.Concurrent)
		public void handleSubTestEvent(SubTestEvent event) {
            // do something more expensive here
		}

This handler will receive all messages of type SubTestEvent or any subtype sequentially,
given that they pass the specified filters. This handler will be invoked before the formerly
defined one, since it specifies a higher priority

		// this handler will receive messages of type SubTestEvent
        // or any of its sub types that passe the given filter(s)
        @Listener(priority = 10,
                  dispatch = Mode.Synchronous,
                  filters = {@Filter(Filters.SpecialEvent.class)})
        public void handleFiltered(SubTestEvent event) {
           //do something special here
        }


        @Listener(dispatch = Mode.Synchronous, rejectSubtypes = true)
        @Enveloped(messages = {TestEvent.class, TestEvent2.class})
        public void handleVariousEvents(MessageEnvelope envelope) {
            // the envelope will contain either an instance of TestEvent or TestEvent2
            // if rejectSubtypes were set to 'false' (default) also subtypes of TestEvent or TestEvent2 would be allowed
        }



<h2>Enveloped message handlers</h2>

Since one parameter (the message) does not offer a great deal of flexibility if different types
of messages should be consumed, there exists the possibility to wrap a message inside an envelope.
An enveloped message handler specifies the message type it consumes by using the @Enveloped annotation
in addition to the @Listener annotation. All configurations of @Listener apply to each of the specified
message types.

        @Listener(dispatch = Mode.Synchronous, rejectSubtypes = true)
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
a super class by using the "enabled" property on the overridden method.
If a class overrides a method that is configured as a message handler in one of its super classes
it is still considered a message handler but of course the implementation of the overriding class
will be used.



