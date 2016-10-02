package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.subscription.MessageEnvelope;

/**
 * The enveloped dispatcher will wrap published messages in an envelope before
 * passing them to their configured dispatcher.
 * <p/>
 * All enveloped message handlers will have this dispatcher in their chain
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class EnvelopedMessageDispatcher extends DelegatingMessageDispatcher {


    public EnvelopedMessageDispatcher(IMessageDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void dispatch(MessagePublication publication, Object message, Iterable listeners){
        getDelegate().dispatch(publication, new MessageEnvelope(message), listeners);
    }
}
