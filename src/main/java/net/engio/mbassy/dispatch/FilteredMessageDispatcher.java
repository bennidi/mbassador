package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.common.ConcurrentSet;
import net.engio.mbassy.listener.IMessageFilter;

/**
 * A dispatcher that implements message filtering based on the filter configuration
 * of the associated message handler. It will delegate message delivery to another
 * message dispatcher after having performed the filtering logic.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class FilteredMessageDispatcher extends DelegatingMessageDispatcher {

    private final IMessageFilter[] filter;

    public FilteredMessageDispatcher(IMessageDispatcher dispatcher) {
        super(dispatcher);
        this.filter = dispatcher.getContext().getHandlerMetadata().getFilter();
    }

    private boolean passesFilter(Object message) {

        if (filter == null) {
            return true;
        } else {
            for (int i = 0; i < filter.length; i++) {
                if (!filter[i].accepts(message, getContext().getHandlerMetadata())) {
                    return false;
                }
            }
            return true;
        }
    }


    @Override
    public void dispatch(MessagePublication publication, Object message, ConcurrentSet listeners) {
        if (passesFilter(message)) {
            getDelegate().dispatch(publication, message, listeners);
        }
    }

}
