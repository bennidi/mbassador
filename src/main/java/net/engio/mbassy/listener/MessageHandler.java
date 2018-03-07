package net.engio.mbassy.listener;

import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.dispatch.el.ElFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Any method in any class annotated with the @Handler annotation represents a message handler. The class that contains
 * the handler is called a  message listener and more generally, any class containing a message handler in its class hierarchy
 * defines such a message listener.
 *
 * @author bennidi
 *         Date: 11/14/12
 */
public class MessageHandler {

    public static final class Properties{

        public static final String HandlerMethod = "handler";
        public static final String InvocationMode = "invocationMode";
        public static final String Filter = "filter";
        public static final String Condition = "condition";
        public static final String Enveloped = "envelope";
        public static final String HandledMessages = "messages";
        public static final String IsSynchronized = "synchronized";
        public static final String Listener = "listener";
        public static final String AcceptSubtypes = "subtypes";
        public static final String Priority = "priority";
        public static final String Invocation = "invocation";

        /**
         * Create the property map for the {@link MessageHandler} constructor using the default objects.
         *
         * @param handler  The handler annotated method of the listener
         * @param handlerConfig The annotation that configures the handler
         * @param filter   The set of preconfigured filters if any
         * @param listenerConfig The listener metadata
         * @return  A map of properties initialized from the given parameters that will conform to the requirements of the
         *         {@link MessageHandler} constructor.
         */
        public static final Map<String, Object> Create(Method handler,
                                                       Handler handlerConfig,
                                                       Enveloped enveloped,
                                                       IMessageFilter[] filter,
                                                       MessageListener listenerConfig){
            if(handler == null){
                throw new IllegalArgumentException("The message handler configuration may not be null");
            }
            if(filter == null){
                filter = new IMessageFilter[]{};
            }
            Class[] handledMessages = enveloped != null
                    ? enveloped.messages()
                    : handler.getParameterTypes();
            handler.setAccessible(true);
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(HandlerMethod, handler);
            // add EL filter if a condition is present
            if(handlerConfig.condition().length() > 0){
                if (!ElFilter.isELAvailable()) {
                    throw new IllegalStateException("A handler uses an EL filter but no EL implementation is available.");
                }

                IMessageFilter[] expandedFilter = new IMessageFilter[filter.length + 1];
                for(int i = 0; i < filter.length ; i++){
                   expandedFilter[i] = filter[i];
                }
                expandedFilter[filter.length] = new ElFilter();
                filter = expandedFilter;
            }
            properties.put(Filter, filter);
            properties.put(Condition, cleanEL(handlerConfig.condition()));
            properties.put(Priority, handlerConfig.priority());
            properties.put(Invocation, handlerConfig.invocation());
            properties.put(InvocationMode, handlerConfig.delivery());
            properties.put(Enveloped, enveloped != null);
            properties.put(AcceptSubtypes, !handlerConfig.rejectSubtypes());
            properties.put(Listener, listenerConfig);
            properties.put(IsSynchronized, ReflectionUtils.getAnnotation( handler, Synchronized.class) != null);
            properties.put(HandledMessages, handledMessages);
            return properties;
        }

        private static String cleanEL(String expression) {

            if (!expression.trim().startsWith("${") && !expression.trim().startsWith("#{")) {
                expression = "${"+expression+"}";
            }
            return expression;
        }
    }


    private final Method handler;

    private final IMessageFilter[] filter;

	private final String condition;
    
    private final int priority;

    private final Class<? extends HandlerInvocation> invocation;

    private final Invoke invocationMode;

    private final boolean isEnvelope;

    private final Class[] handledMessages;

    private final boolean acceptsSubtypes;

    private final MessageListener listenerConfig;

    private final boolean isSynchronized;


    public MessageHandler(Map<String, Object> properties){
        super();
        validate(properties);
        this.handler = (Method)properties.get(Properties.HandlerMethod);
        this.filter = (IMessageFilter[])properties.get(Properties.Filter);
        this.condition = (String)properties.get(Properties.Condition);
        this.priority = (Integer)properties.get(Properties.Priority);
        this.invocation = (Class<? extends HandlerInvocation>)properties.get(Properties.Invocation);
        this.invocationMode = (Invoke)properties.get(Properties.InvocationMode);
        this.isEnvelope = (Boolean)properties.get(Properties.Enveloped);
        this.acceptsSubtypes = (Boolean)properties.get(Properties.AcceptSubtypes);
        this.listenerConfig = (MessageListener)properties.get(Properties.Listener);
        this.isSynchronized = (Boolean)properties.get(Properties.IsSynchronized);
        this.handledMessages = (Class[])properties.get(Properties.HandledMessages);
    }

    private void validate(Map<String, Object> properties){
        // define expected types of known properties
        Object[][] expectedProperties = new Object[][]{
                new Object[]{Properties.HandlerMethod, Method.class },
                new Object[]{Properties.Priority, Integer.class },
                new Object[]{Properties.Invocation, Class.class },
                new Object[]{Properties.Filter, IMessageFilter[].class },
                new Object[]{Properties.Condition, String.class },
                new Object[]{Properties.Enveloped, Boolean.class },
                new Object[]{Properties.HandledMessages, Class[].class },
                new Object[]{Properties.IsSynchronized, Boolean.class },
                new Object[]{Properties.Listener, MessageListener.class },
                new Object[]{Properties.AcceptSubtypes, Boolean.class }
        };
        // ensure types match
        for(Object[] property : expectedProperties){
            if (properties.get(property[0]) == null || !((Class)property[1]).isAssignableFrom(properties.get(property[0]).getClass()))
                throw new IllegalArgumentException("Property " + property[0] + " was expected to be not null and of type " + property[1]
                        + " but was: " + properties.get(property[0]));
        }
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType){
        return ReflectionUtils.getAnnotation(handler,annotationType);
    }

    public boolean isSynchronized(){
        return isSynchronized;
    }

    public boolean useStrongReferences(){
        return listenerConfig.useStrongReferences();
    }

    public boolean isFromListener(Class listener){
        return listenerConfig.isFromListener(listener);
    }

    public boolean isAsynchronous() {
        return invocationMode.equals(Invoke.Asynchronously);
    }

    public boolean isFiltered() {
        return filter.length > 0 || (condition != null && condition.trim().length() > 0);
    }

    public int getPriority() {
        return priority;
    }

    public Method getMethod() {
        return handler;
    }

    public IMessageFilter[] getFilter() {
        return filter;
    }
    
    public String getCondition() {
    	return this.condition;
    }

    public Class[] getHandledMessages() {
        return handledMessages;
    }

    public boolean isEnveloped() {
        return isEnvelope;
    }

    public Class<? extends HandlerInvocation> getHandlerInvocation(){
        return invocation;
    }

    public boolean handlesMessage(Class<?> messageType) {
        for (Class<?> handledMessage : handledMessages) {
            if (handledMessage.equals(messageType)) {
                return true;
            }
            if (handledMessage.isAssignableFrom(messageType) && acceptsSubtypes()) {
                return true;
            }
        }
        return false;
    }

    public boolean acceptsSubtypes() {
        return acceptsSubtypes;
    }

}
