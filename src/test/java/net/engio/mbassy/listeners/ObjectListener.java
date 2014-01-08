package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class ObjectListener {

    private List handledMessages = Collections.synchronizedList(new LinkedList());

    @Handler(priority = Integer.MAX_VALUE)
    public void handle(Object message){
        handledMessages.add(message);
    }

}
