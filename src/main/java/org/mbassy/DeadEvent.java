package org.mbassy;

public class DeadEvent
{
    private Object event;

    public DeadEvent(Object event)
    {
        this.event = event;
    }

    public Object getEvent()
    {
        return this.event;
    }
}
