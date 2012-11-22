package org.mbassy.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This factory will create a list of beans according to some specified configuration.
 * It can be used to setup different test scenarios.
 *
 * @author bennidi
 *         Date: 11/22/12
 */
public class ListenerFactory {

    private Map<Class, Integer> requiredBeans = new HashMap<Class, Integer>();



    public ListenerFactory create(int numberOfInstance, Class clazz){
        requiredBeans.put(clazz, numberOfInstance);
        return this;
    }


    public List<Object> build() throws Exception{
        List<Object> beans = new LinkedList<Object>();
        for(Class clazz : requiredBeans.keySet()){
            int numberOfRequiredBeans = requiredBeans.get(clazz);
            for(int i = 0; i < numberOfRequiredBeans; i++){
                beans.add(clazz.newInstance());
            }
        }
        return beans;
    }


}
