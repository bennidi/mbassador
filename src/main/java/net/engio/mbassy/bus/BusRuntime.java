package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.MissingPropertyException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Message bus implementations potentially vary in the features they provide and consequently in the components and properties
 * they expose. The runtime is a container for all those dynamic properties and components and is meant to be passed around
 * between collaborating objects such that they may access the different functionality provided by the bus implementation
 * they all belong to.
 *
 * It is the responsibility of the bus implementation to create and configure the runtime according to its capabilities,
 *
 */
public class BusRuntime {

    private PubSubSupport provider;

    private Map<String, Object> properties = new HashMap<String, Object>();

    public BusRuntime(PubSubSupport provider) {
        this.provider = provider;
    }

    public <T> T get(String key){
         if(!contains(key))
             throw new MissingPropertyException("The property " + key + " is not available in this runtime");
         else return (T) properties.get(key);
     }

    public PubSubSupport getProvider(){
        return provider;
    }

    public Collection<String> getKeys(){
        return properties.keySet();
    }

    public BusRuntime add(String key, Object property){
        properties.put(key, property);
        return this;
    }

    public boolean contains(String key){
        return properties.containsKey(key);
    }


}
