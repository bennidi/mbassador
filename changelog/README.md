### 1.3.3

+ TODO
  + Remove IBusConfiguration (merge with BusConfiguration)
  + Introduce @Asynchronous as alternative to delivery mode
  + @Enveloped.messages -> @Enveloped.types
  + MessageEnvelope -> Envelope
  
### 1.3.2
  
  + Merged PR #153: Support of handler definition in interfaces

### 1.3.1

+ Merged PR #150: Filter definition as reusable annotation

### 1.3.0
 + Non-Breaking API changes
   + Extended IMessagePublication to allow for error reporting using `hasError()` and `getError()`
   + Any publication method now returns an IMessagePublication object. This resolves [PR-127](../pull/127). Any dispatched
    message publication can now be inspected for execution error. Does not support collection of multiple errors due to implied
    GC and memory allocation overhead in high-throughput scenarios.
 + Breaking API changes
    + Added MessagePublication to IHandlerInvocation.invoke(...)
    + Added MessagePublication to IMessageDispatcher.dispatch(...)

### 1.2.4.2
 + Updated pom. Now using nexus-staging plugin
 + Removed pmd
 + Fixed #135 and #136

### [1.2.4](http://github.com/bennidi/mbassador/milestones/1.2.4)
 + API-Changes:
   + Remove IBusConfiguration.{handleError,addConfigurationErrorHandler} => Configuration errors are communicated as RuntimeExceptions
   + Removed BusFactory => Use explicit feature based construction with BusConfiguration 
 + Integrated JaCoCo test coverage report => run `mvn clean test -Djacoco`  


### [1.2.3](http://github.com/bennidi/mbassador/milestones/1.2.2)
 + Upgraded to Maven 3
   + Upgraded all plugins to most recent version
   + Fixed all issues that produced warnings on build output
 + reduced visibility of `AbstractPubSubSupport.handlePublication` error from `public` to `protected`
 + Integrated [performance improvements](https://github.com/bennidi/mbassador/pull/125) made by dorkbox
+ __API-Changes:__
   + Moved method addPublicationErrorHandler from `IMessageBus` to `IBusConfiguration`
   + Default constructor of `MBassador` has no `IPublicationErrorHandler` registered and will 
   fall back to console logging. See [#106](http://github.com/bennidi/mbassador/issues/106), [#107](http://github.com/bennidi/mbassador/issues/107) 

### [1.2.2](http://github.com/bennidi/mbassador/milestones/1.2.2)
 + Due to technical problems during release creation this version had to be skipped (git tag not removable)
 + The respective release is 1.2.3 


### [1.2.1](http://github.com/bennidi/mbassador/milestones/1.2.1)
 + Centralized handling of common (and arbitrary) properties (see `BusConfiguration#setProperty` and `net.engio.mbassy.bus.config.IBusConfiguration.Properties`)
 + Each bus now has a configurable id and respective #toString() implementation (useful for debugging)
 + Each bus now has a default logger (System.out) for publication errors (exception in handlers) which can be replaced with BusConfiguration#setProperty 
 + __API-Changes:__
   + Interface `IMessageFilter` now receives the SubscriptionContext as second parameter. This gives access to the bus runtime within filter logic (useful for error propagation). -> Change your filters signature. You can access the `MessageHandler` object directly from the context. 
   + Removed deprecated method `BusConfiguration.SyncAsync()` -> Use default constructor or feature based configuration instead
   + Deleted interface `ISyncMessageBus` since it was merely an aggregation of existing interfaces -> Replace with GenericMessagePublicationSupport

### 1.2.0
 + Added support for conditional handlers using Java EL. Thanks to Bernd Rosstauscher for the initial implementation.
 + __BREAKING CHANGES__ in BusConfiguration
   + Complete redesign of configuration setup using Features instead of simple get/set parameters. This will allow
 to flexibly combine features and still be able to exclude those not available in certain environments,for example, threading and reflection in GWT (this will be part of future releases)
   + Properties formerly located in BusConfiguration now moved to their respective Feature class
   + Removed all SyncXX related interfaces and config implementations. There is now only one `BusConfiguration`
 with its corresponding interface which will be used for all types of message bus implementations


### 1.1.10
 + Fixed broken sort order of prioritized handlers (see [#58](http://github.com/bennidi/mbassador/issues/58))
 + Addressed issue #63 by making the constructor of `MessageHandler` use a map of properties and by replacing dependencies to
  all MBassador specific annotations with Java primitives and simple interfaces
 + Small refactorings (moved stuff around to have cleaner packaging)
 + MessageBus.getExecutor() is now deprecated and will be removed with next release -> use the runtime to get access to it.
 + Introduced BusFactory with convenience methods for creating bus instances for different message dispatching scenarios like
 asynchronous FIFO (asynchronous message publications guaranteed to be delivered in the order they occurred)
 + Renamed runtime property of `BusRuntime` "handler.async-service" to "handler.async.executor"

### 1.1.9

 + Fixed memory leak reported in [#53](http://github.com/bennidi/mbassador/issues/53)

### 1.1.8

 + Internal refactorings and code improvements
 + Fixed [#44](http://github.com/bennidi/mbassador/issues/44) [#45](http://github.com/bennidi/mbassador/issues/45) [#47](http://github.com/bennidi/mbassador/issues/47)
 + NOTE: This release has a known issue with weak references which introduces a memory leak and is fixed in 1.1.9. The
 version 1.1.8 is not available from the central repository


### 1.1.7

 + Console Logger not added to message bus instances by default -> use addErrorHandler(IPublicationErrorHandler.ConsoleLogger)
 + Fixed race conditions in net.engio.mbassy.subscription.Subscription and of WeakConcurrentSet.contains()
 + Improved message hierarchy handling: Now interfaces, enums , (abstract) classes should work in all combinations
 + Prevented dispatcher threads from dying on exceptions
 + Improved test-infrastructure and increased test-coverage
 + Thanks for your feedback!

### 1.1.6

 + Added support for choosing between strong and weak references using the new @Listener annotation. @Listener can be
 added to any class that defines message handlers and allows to configure which reference type is used
 + Custom handler invocations: It is possible to provide a custom handler invocation for each message handler, see "invocation"
 property of @Handler
 + Changed packaging to "bundle" to support OSGI environments
 + Synchronization of message handlers via @Synchronized: Handlers that are not thread-safe can be synchronized to guarantee
  that only one thread at a time can invoke that handler
 + Created a message bus implementation that does not use threading to support use in non-multi-threaded environments like GWT,
 see ISyncMessageBus

### 1.1.3

 + Added support for FilteredMessage event
 + Renamed @Listener to @Handler and DeadEvent to DeadMessage to increase alignment with the established terminology.
 Sorry for the inconvenience since this will lead to compile errors but good old find&replace will do
 + Repackaging and refactoring of some parts
 + Introduced message publication factories as configurable components to make MBassador more extensible/customizable
 + Added more documentation and unit tests

### 1.1.1

 + Added support for DeadMessage event
 + Introduced new property to @Handler annotation that allows to activate/deactivate any message handler
 + Full support of proxies created by cglib
 + Message handler inheritance changed! See wiki page about handler definition for more details.
 + Changed @Handler property "dispatch" to "delivery" and renamed the associated enumeration values to
   more precisely indicate their meaning
 + Added more unit tests

### 1.1.0

First stable release!

 + Refactoring and repackaging
 + More exhaustive unit tests
 + Installation from the central repository

### 1.0.6.RC

 + Fixed behaviour with capacity bound blocking queue such that there now are two methods to schedule a message
 asynchronously. One will block until capacity becomes available, the other will timeout after a specified amount of
 time.
 +  Additional unit tests

### 1.0.5.RC

 + Added MessageEnvelope and @Enveloped annotation to configure handlers that might receive arbitrary message type
 + Added handler configuration property to @Handler annotation to move from message filtering to more specific implementation
 of this feature

### 1.0.4.RC

  + Introduced BusConfiguration as a central class to encapsulate configurational aspects
