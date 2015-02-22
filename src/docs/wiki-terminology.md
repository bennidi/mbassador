<h2>Terminology</h2>
To avoid confusion and increase precision of the available documentation a common vocabulary of the most relevant concepts is necessary. Specifically, the terms "event" and "message" have their own definition within the context of the message bus system and as such require
some clarification.

<h3>Message</h3>
A message is an object used for communication between a sender and a set of receivers. Other libraries established the term "event" which essentially refers to the same idea (an event occurs at some point in the system and is published to other components such that they might react to it).  
MBassador uses the term `message` instead of `event` since the object sent over the wire does not necessarily represent an event. It might merely represent data to be processed, e.g. stored or transformed.

A message can be any object, no restrictions or assumptions are made. A message can be sent by any object that has access to the bus
and is delivered to all registered handlers consuming that type of message.

<h3>Message handler</h3>
A message handler is a method that defines exactly one parameter (the message or a message envelope) and is marked with @Handler. A handler has a message type that is implicitly defined in the method signature (the parameter type). A message handler will be invoked for each message that has a compatible type.

<h3>Message listener</h3>
A class defining one or more message handlers and that has been subscribed at the message bus is referred to as (message) listener.

<h3>Subscription</h3>
Subscription is the process of adding a listener to the message bus, such that it might receive messages. It is used interchangeably with the
term "registration"

<h3>Message publication|Message dispatch</h3>
The process of delivering a message from the sender to all registered listeners is called message publication.
The initial phase of this process, that lasts until the message is actually delivered to the handlers is called message dispatch.
The distinction is necessarily drawn as all method publications share a common scheme but may vary in the way how the dispatching works.

<h3>Event</h3>
The term "event" refers to events that can occur during message publication. Currently there are two types of events:

 + DeadMessage: Whenever a message is published and no listeners exist that defined matching handlers, a DeadMessage event will be created and published
 using the common message publication mechanism. Listeners with handlers for DeadEvent can be subscribed to listen for and react to dead
 + FilteredMessage: Since handlers can define filters to narrow the set of messages it consumes, it is possible that a message is not delivered
 to any handler. In such a case the FilteredMessage event is published,

