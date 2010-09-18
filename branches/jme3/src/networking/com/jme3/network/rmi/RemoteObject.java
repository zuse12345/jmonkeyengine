package com.jme3.network.rmi;

import com.jme3.network.connection.Client;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Contains various meta-data about an RMI interface.
 *
 * @author Kirill Vainer
 */
public class RemoteObject implements InvocationHandler {

    /**
     * Object ID
     */
    short objectId;

    /**
     * Contains {@link MethodDef method definitions} for all exposed
     * RMI methods in the remote RMI interface.
     */
    MethodDef[] methodDefs;

    /**
     * Maps from methods locally retrieved from the RMI interface to
     * a method ID.
     */
    HashMap<Method, Integer> methodMap = new HashMap<Method, Integer>();

    /**
     * The {@link ObjectStore} which stores this RMI interface.
     */
    ObjectStore store;
    
    /**
     * The client who exposed the RMI interface, or null if the server
     * exposed it.
     */
    Client client;

    public RemoteObject(ObjectStore store, Client client){
        this.store = store;
        this.client = client;
    }

    /**
     * Generates mappings from the given interface into the remote RMI
     * interface's implementation.
     *
     * @param interfaceClass
     */
    public void loadMethods(Class<?> interfaceClass){
        for (int i = 0; i < methodDefs.length; i++){
            try {
                MethodDef methodDef = methodDefs[i];
                Method method = interfaceClass.getMethod(methodDef.name, methodDef.paramTypes);
                methodMap.put(method, i);
            } catch (NoSuchMethodException ex){
                // ignore undefined methods
            }
        }
    }

    /**
     * Callback from InvocationHandler.
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return store.invokeRemoteMethod(this, method, args);
    }

}
