package net.engio.mbassy.listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: benjamin
 * Date: 6/26/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class Listeners {

    private static final List<Class> Synchronous = Collections.unmodifiableList(Arrays.asList(new Class[]{
            MessagesListener.DefaultListener.class,
            IMessageListener.DefaultListener.class,
            StandardMessageListener.DefaultListener.class,
            MultipartMessageListener.DefaultListener.class,
            ICountableListener.DefaultListener.class,
            IMultipartMessageListener.DefaultListener.class}));

    private static final List<Class> SubtypeRejecting = Collections.unmodifiableList(Arrays.asList(new Class[]{
            MessagesListener.NoSubtypesListener.class,
            IMessageListener.NoSubtypesListener.class,
            StandardMessageListener.NoSubtypesListener.class,
            MultipartMessageListener.NoSubtypesListener.class,
            ICountableListener.NoSubtypesListener.class,
            IMultipartMessageListener.NoSubtypesListener.class}));

    private static final List<Class> NoHandlers = Collections.unmodifiableList(Arrays.asList(new Class[]{
            MessagesListener.DisabledListener.class,
            IMessageListener.DisabledListener.class,
            StandardMessageListener.DisabledListener.class,
            MultipartMessageListener.DisabledListener.class,
            ICountableListener.DisabledListener.class,
            IMultipartMessageListener.DisabledListener.class,
            Object.class,String.class}));


    private static final List<Class> HandlesIMessage = Collections.unmodifiableList(Arrays.asList(new Class[]{
            IMessageListener.DefaultListener.class,
            IMessageListener.NoSubtypesListener.class,
            IMultipartMessageListener.DefaultListener.class,
            IMultipartMessageListener.NoSubtypesListener.class,
            MessagesListener.DefaultListener.class,
            MessagesListener.NoSubtypesListener.class,
            StandardMessageListener.DefaultListener.class,
            StandardMessageListener.NoSubtypesListener.class,
            MultipartMessageListener.DefaultListener.class,
            MultipartMessageListener.NoSubtypesListener.class}));

    private static final List<Class> HandlesStandardessage = Collections.unmodifiableList(Arrays.asList(new Class[]{
            IMessageListener.DefaultListener.class,
            ICountableListener.DefaultListener.class,
            StandardMessageListener.DefaultListener.class,
            StandardMessageListener.NoSubtypesListener.class}));


    public static Collection<Class> synchronous(){
        return Synchronous;
    }

    public static Collection<Class> subtypeRejecting(){
        return SubtypeRejecting;
    }

    public static Collection<Class> noHandlers(){
        return NoHandlers;
    }

    public static Collection<Class> handlesIMessage(){
        return HandlesIMessage;
    }

    public static Collection<Class> handlesStandardMessage(){
        return HandlesStandardessage;
    }


    public static Collection<Class> join(Collection<Class>...listenerSets){
        Set<Class> join = new HashSet<Class>();
        for(Collection<Class> listeners : listenerSets) {
            join.addAll(listeners);
        }
        for(Collection<Class> listeners : listenerSets) {
            join.retainAll(listeners);
        }
        return join;
    }




}
