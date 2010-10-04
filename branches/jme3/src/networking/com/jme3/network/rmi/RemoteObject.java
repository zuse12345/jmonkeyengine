package com.jme3.network.rmi;


import com.jme3.network.connection.Client;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

    private boolean methodEquals(MethodDef methodDef, Method method){
        Class<?>[] interfaceTypes = method.getParameterTypes();
        Class<?>[] defTypes       = methodDef.paramTypes;

        if (interfaceTypes.length == defTypes.length){
            for (int i = 0; i < interfaceTypes.length; i++){
                if (!defTypes[i].isAssignableFrom(interfaceTypes[i])){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Generates mappings from the given interface into the remote RMI
     * interface's implementation.
     *
     * @param interfaceClass
     */
    public void loadMethods(Class<?> interfaceClass){
        HashMap<String, ArrayList<Method>> nameToMethods
                = new HashMap<String, ArrayList<Method>>();

        for (Method method : interfaceClass.getDeclaredMethods()){
            ArrayList<Method> list = nameToMethods.get(method.getName());
            if (list == null){
                list = new ArrayList<Method>();
                nameToMethods.put(method.getName(), list);
            }
            list.add(method);
        }

        mapping_search: for (int i = 0; i < methodDefs.length; i++){
            MethodDef methodDef = methodDefs[i];
            ArrayList<Method> methods = nameToMethods.get(methodDef.name);
            if (methods == null)
                continue;
            
            for (Method method : methods){
                if (methodEquals(methodDef, method)){
                    methodMap.put(method, i);
                    continue mapping_search;
                }
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
