package org.mbassy;

public class DeadEvent implements IEventFunction
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
