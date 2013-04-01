Note: Please refer to the terminology wiki page before reading the following explanations..

A message bus offers facilities for publishing messages to registered listeners. Messages can be dispatched
synchronously or asynchronously and the dispatch mechanism can by controlled for each message handler and per message publication.

Each message publication is isolated from all other running publications such that it does not interfere with them.
Hence, the bus expects message handlers to be stateless as they may be invoked concurrently if multiple
messages of the same type get published asynchronously.

Messages are published to all listeners that accept the type or super type of the published message. Additionally
a message handler may define filters to narrow the set of messages that it accepts.

Subscribed listeners are available to all pending message publications that have not yet started processing.
Any message listener may only be subscribed once - subsequent subscriptions of an already subscribed message listener
will be silently ignored.

The basic contract of the bus is that it will deliver a specific message exactly once to each of the subscribed message handlers.
Currently, message handlers will be invoked in inverse sequence of subscription but any
client using this bus should not rely on this assumption.

By default, the bus uses weak references to all listeners such that registered listeners do not need to
be explicitly unregistered to be eligible for garbage collection. Dead (garbage collected) listeners are
removed on-the-fly as messages get published. It is possible to enable the use of strong references on the message handler
level.

Unsubscribing a listener means removing all subscribed message handlers of that listener. This remove operation
immediately effects all running publications processes -> A removed listener will under no circumstances receive any message publications.
A listener is considered removed after the unsubscribe(Object) call returned.Any running message publication that has not yet delivered
the message to the recently removed listener will not see the listener after the remove operation completed.
