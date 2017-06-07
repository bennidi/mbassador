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

        public static final String HANDLER_METHOD = "handler";
        public static final String INVOCATION_MODE = "invocationMode";
        public static final String FILTER = "filter";
        public static final String CONDITION = "condition";
        public static final String ENVELOPED = "envelope";
        public static final String HANDLED_MESSAGES = "messages";
        public static final String IS_SYNCHRONIZED = "synchronized";
        public static final String LISTENER = "listener";
        public static final String ACCEPT_SUBTYPES = "subtypes";
        public static final String PRIORITY = "priority";
        public static final String INVOCATION = "invocation";

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
                                                       IMessageFilter[] filter,
                                                       MessageListener listenerConfig){
            if(handler == null){
                throw new IllegalArgumentException("The message handler configuration may not be null");
            }
            if(filter == null){
                filter = new IMessageFilter[]{};
            }
            Enveloped enveloped = ReflectionUtils.getAnnotation( handler, Enveloped.class );
            Class[] handledMessages = enveloped != null
                    ? enveloped.messages()
                    : handler.getParameterTypes();
            handler.setAccessible(true);
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(HANDLER_METHOD, handler);
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
            properties.put(FILTER, filter);
            properties.put(CONDITION, cleanEL(handlerConfig.condition()));
            properties.put(PRIORITY, handlerConfig.priority());
            properties.put(INVOCATION, handlerConfig.invocation());
            properties.put(INVOCATION_MODE, handlerConfig.delivery());
            properties.put(ENVELOPED, enveloped != null);
            properties.put(ACCEPT_SUBTYPES, !handlerConfig.rejectSubtypes());
            properties.put(LISTENER, listenerConfig);
            properties.put(IS_SYNCHRONIZED, ReflectionUtils.getAnnotation( handler, Synchronized.class) != null);
            properties.put(HANDLED_MESSAGES, handledMessages);
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
        this.handler = (Method)properties.get(Properties.HANDLER_METHOD);
        this.filter = (IMessageFilter[])properties.get(Properties.FILTER);
        this.condition = (String)properties.get(Properties.CONDITION);
        this.priority = (Integer)properties.get(Properties.PRIORITY);
        this.invocation = (Class<? extends HandlerInvocation>)properties.get(Properties.INVOCATION);
        this.invocationMode = (Invoke)properties.get(Properties.INVOCATION_MODE);
        this.isEnvelope = (Boolean)properties.get(Properties.ENVELOPED);
        this.acceptsSubtypes = (Boolean)properties.get(Properties.ACCEPT_SUBTYPES);
        this.listenerConfig = (MessageListener)properties.get(Properties.LISTENER);
        this.isSynchronized = (Boolean)properties.get(Properties.IS_SYNCHRONIZED);
        this.handledMessages = (Class[])properties.get(Properties.HANDLED_MESSAGES);
    }

    private void validate(Map<String, Object> properties){
        // define expected types of known properties
        Object[][] expectedProperties = new Object[][]{
                new Object[]{Properties.HANDLER_METHOD, Method.class },
                new Object[]{Properties.PRIORITY, Integer.class },
                new Object[]{Properties.INVOCATION, Class.class },
                new Object[]{Properties.FILTER, IMessageFilter[].class },
                new Object[]{Properties.CONDITION, String.class },
                new Object[]{Properties.ENVELOPED, Boolean.class },
                new Object[]{Properties.HANDLED_MESSAGES, Class[].class },
                new Object[]{Properties.IS_SYNCHRONIZED, Boolean.class },
                new Object[]{Properties.LISTENER, MessageListener.class },
                new Object[]{Properties.ACCEPT_SUBTYPES, Boolean.class }
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
        return invocationMode.equals(Invoke.ASYNCHRONOUSLY);
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
