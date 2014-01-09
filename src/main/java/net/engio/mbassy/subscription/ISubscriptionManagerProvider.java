package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.listener.MetadataReader;

public interface ISubscriptionManagerProvider {
	SubscriptionManager createManager(MetadataReader reader,
			SubscriptionFactory factory, BusRuntime runtime);
}
