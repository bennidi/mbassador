package net.engio.mbassy.messages;

/**
 * Interface analogous to IMessage. Exists to test more complex class/interface hierarchies
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public interface ICountable {

    void reset();

    void handled(Class listener);

    int getTimesHandled(Class listener);
}
