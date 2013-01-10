package net.engio.mbassy.subscription;

import java.sql.Timestamp;

/**
 * A message envelope is used to wrap messages of arbitrary type such that a handler
 * my receive messages of different types.
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class MessageEnvelope {

    private Timestamp tsCreated = new Timestamp(System.currentTimeMillis());

    private Object message;


    public MessageEnvelope(Object message) {
        this.message = message;
    }


    public <T> T getMessage(){
        return (T)message;
    }
}
