package net.engio.mbassy.messages;

/**
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public interface IMessage {

    void reset();

    void handled(Class listener);

    int getTimesHandled(Class listener);

}
