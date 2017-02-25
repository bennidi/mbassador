package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Listener(references = References.Weak)
public class ObjectListener {

    private List handledMessages = Collections.synchronizedList(new LinkedList());

    @Handler(priority = Integer.MAX_VALUE)
    public void handle(Object message){
        handledMessages.add(message);
    }

}
