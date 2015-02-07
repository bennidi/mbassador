package net.engio.mbassy.disruptor;

/**
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class MessageHolder {
    public MessageType messageType = MessageType.ONE;

    public Object message1 = null;
    public Object message2 = null;
    public Object message3 = null;
    public Object[] messages = null;

    public MessageHolder() {
    }
}