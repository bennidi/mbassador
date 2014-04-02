#Tests
Asyncbus.shutdown() -> no test coverage
EnvelopedMessageDispatcher -> not tested at all

#Refactorings


#Improvements
Prio 1: Validation of handlers
    ERROR:Handler with mismatching parameter types
    ERROR:Interfaces + rejectSubtypes
    WARN:@Synchronized only for some handlers of a given listener
Prio 2: Lifecycle Callbacks = Implement in MessagePublication (BeforeStart,AfterCompletion)


#Documentation
Add code examples Javadoc of main classes
Describe 1-Thread FIFO scheme with async dispatch
Explain how MBassador can be extended easily using delegation
Refer to Spring integration component
Creating bus hierarchies
How to make sender part of the message publication
How to add global filtering by means of delegation
