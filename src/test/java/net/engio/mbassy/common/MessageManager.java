package net.engio.mbassy.common;

import net.engio.mbassy.messages.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 *
 */
public class MessageManager {
	private static final Logger LOG =
			LoggerFactory.getLogger(MessageManager.class);


    private StrongConcurrentSet<MessageContext> messages = new StrongConcurrentSet();


    public <T extends IMessage> T create(Class<T> messageType, int expectedCount, Class ...listeners){
        T message;
        try {
            message = messageType.newInstance();
            register(message, expectedCount, listeners);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    public <T extends IMessage> T createAndTrack(Class<T> messageType, int expectedCount, Collection<Class> listeners){
        T message;
        try {
            message = messageType.newInstance();
            register(message, expectedCount, listeners);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    public <T extends IMessage> void register(T message, int expectedCount, Class ...listeners){
        try {
            messages.add(new MessageContext(expectedCount, message, listeners));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends IMessage> void register(T message, int expectedCount, Collection<Class> listeners){
        try {
            messages.add(new MessageContext(expectedCount, message, listeners));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void waitForMessages(int timeoutInMs){
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start < timeoutInMs && messages.size() > 0){
            // check each created message once
            for(MessageContext messageCtx : messages){
                boolean handledCompletely = true;
                for(Class listener : messageCtx.getListeners()){
                    handledCompletely &= messageCtx.getMessage().getTimesHandled(listener) == messageCtx.getExpectedCount();
                }
                // remove the ones that were handled as expected
                if(handledCompletely){
                    logSuccess(messageCtx);
                    messages.remove(messageCtx);
                }

            }
            pause(100);
        }
        if(messages.size() > 0){
            logFailingMessages(messages);
            org.junit.Assert.fail("Message were not fully processed in given time");
        }


    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

   private void logSuccess(MessageContext mCtx){
       LOG.info(mCtx.getMessage().getClass().getSimpleName() + " successfully handled " + mCtx.getExpectedCount() + " times by " + mCtx.printListeners());
   }



    private void logFailingMessages(StrongConcurrentSet<MessageContext> failing){
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Failing messages:\n");
        for(MessageContext failingMessage : failing)
            errorMessage.append(failingMessage);
        LOG.info(errorMessage.toString());
    }

    private class MessageContext{

        private long expectedCount;
        private IMessage message;
        private Class[] listeners;

        private MessageContext(long expectedCount, IMessage message, Class[] listeners) {
            this.expectedCount = expectedCount;
            this.message = message;
            this.listeners = listeners;
        }

        private MessageContext(long expectedCount, IMessage message, Collection<Class> listeners) {
            this.expectedCount = expectedCount;
            this.message = message;
            this.listeners = listeners.toArray(new Class[]{});
        }

        private long getExpectedCount() {
            return expectedCount;
        }

        private IMessage getMessage() {
            return message;
        }

        private Class[] getListeners() {
            return listeners;
        }

        private String printListeners(){
            StringBuilder listenersAsString = new StringBuilder();
            for(Class listener : listeners){
                listenersAsString.append(listener.getName());
                listenersAsString.append(",");
            }
            return listenersAsString.toString();
        }

        @Override
        public String toString() {
            // TODO: actual count of listeners
            return message.getClass().getSimpleName() + "{" +
                    "expectedCount=" + expectedCount +
                    ", listeners=" + printListeners() +
                    '}';
        }
    }


}
