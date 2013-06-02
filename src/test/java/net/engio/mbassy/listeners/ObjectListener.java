package net.engio.mbassy.listeners;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.listener.Handler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class ObjectListener {

    private List handledMessages = Collections.synchronizedList(new LinkedList());

    @Handler
    public void handle(Object message){
        handledMessages.add(message);
    }

}
