package com.jme3.network.rmi;


import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.ConnectionListener;
import com.jme3.network.events.MessageListener;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializer;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

public class ObjectStore implements MessageListener, ConnectionListener {

    private static final class Invocation {
        Object retVal;
        boolean available = false;
    }

    private Client client;
    private Server server;

    // Local object ID counter
    private short objectIdCounter = 0;
    
    // Local invocation ID counter
    private short invocationIdCounter = 0;

    // Invocations waiting ..
    private IntMap<Invocation> pendingInvocations = new IntMap<Invocation>();
    
    // Objects I share with other people
    private IntMap<LocalInterface> localObjects = new IntMap<LocalInterface>();
    private HashMap<Object, Integer> localObjectsRefs = new HashMap<Object, Integer>();

    // Objects others share with me
    private HashMap<String, RemoteInterface> remoteObjects = new HashMap<String, RemoteInterface>();
    private IntMap<RemoteInterface> remoteObjectsById = new IntMap<RemoteInterface>();

    private final Object recieveObjectLock = new Object();
   
    static {
        Serializer s = RmiSerializer.getInstance();
        Serializer.registerClass(Remote.class, s);
        Serializer.registerClass(RemoteObjectDefMessage.class, s);
        Serializer.registerClass(RemoteMethodCallMessage.class, s);
        Serializer.registerClass(RemoteMethodReturnMessage.class, s);
    }

    public ObjectStore(Client client){
        this.client = client;
        client.addMessageListener(this, RemoteObjectDefMessage.class, 
                                        RemoteMethodCallMessage.class,
                                        RemoteMethodReturnMessage.class);
        client.addConnectionListener(this);

        RmiSerializer.getInstance().setClientStore(this);
    }

    public ObjectStore(Server server){
        this.server = server;
        server.addMessageListener(this, RemoteObjectDefMessage.class, 
                                        RemoteMethodCallMessage.class,
                                        RemoteMethodReturnMessage.class);
        server.addConnectionListener(this);
        
        RmiSerializer.getInstance().setServerStore(this);
    }

    private int createObjectId(){
        int localId  = ++objectIdCounter;
        if (server != null){
            return localId;
        }else{
            return (client.getClientID() << 16) | localId;
        }
    }

    private ObjectDef makeObjectDef(LocalInterface localObj){
        ObjectDef def = new ObjectDef();
        def.objectName = localObj.objectName;
        def.objectId   = localObj.objectId;
        def.methods    = localObj.methods;
        return def;
    }

    private int exposeSharedObject(String name, Object obj){
        if (!(obj instanceof SharedObject))
            throw new IllegalArgumentException("Object must extend SharedObject class");

        return 0;
    }

    private int exposeRemoteInterface(String name, Object obj) throws IOException{
        if (!(obj instanceof Remote))
            throw new IllegalArgumentException("Object must implement Remote interface");

         // Create a local object
        LocalInterface localObj = new LocalInterface();
        localObj.objectName = name;
        localObj.objectId  = objectIdCounter++;
        localObj.theObject = obj;
        localObj.methods   = obj.getClass().getMethods();

        // Put it in the store
        localObjects.put(localObj.objectId, localObj);
        localObjectsRefs.put(obj, localObj.objectId);

        // Inform the others of its existance
        RemoteObjectDefMessage defMsg = new RemoteObjectDefMessage();
        defMsg.objects = new ObjectDef[]{ makeObjectDef(localObj) };

        if (client != null)
            client.send(defMsg);
        else
            server.broadcast(defMsg);

        return localObj.objectId;
    }

    /**
     * Expose object without a name. Returns the objectID.
     */
    public int exposeObject(Object obj) throws IOException{
        if (obj == null)
            throw new NullPointerException();

        return exposeRemoteInterface(null, obj);
    }

    /**
     * Expose a named object.
     * @param name
     * @param obj
     * @throws IOException
     */
    public void exposeObject(String name, Object obj) throws IOException{
        if (obj == null || name == null)
            throw new NullPointerException();
        
        exposeRemoteInterface(name, obj);
    }

    public <T> T getExposedObject(String name, Class<T> type, boolean waitFor) throws InterruptedException{
        RemoteInterface ro = remoteObjects.get(name);
        if (ro == null){
            if (!waitFor)
                throw new RuntimeException("Cannot find remote object named: " + name);
            else{
                do {
                    synchronized (recieveObjectLock){
                        recieveObjectLock.wait();
                    }
                } while ( (ro = remoteObjects.get(name)) == null );
            }
        }
            
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ type }, ro);
        ro.loadMethods(type);
        return (T) proxy;
    }

    public <T> T getExposedObject(int objectId, Class<T> type){
        RemoteInterface ro = remoteObjectsById.get(objectId);
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ type }, ro);
        ro.loadMethods(type);
        return (T) proxy;
    }

    LocalInterface getLocalInterface(int objectId){
        return localObjects.get(objectId);
    }

    RemoteInterface getRemoteObject(int objectId){
        return remoteObjectsById.get(objectId);
    }

//    public int getLocalObjectRef(Object obj) throws IOException{
//        Integer i = localObjectsRefs.get(obj);
//        if (i == null){
//
//        }
//
//        return i;
//    }

    Object invokeRemoteMethod(RemoteInterface remoteObj, Method method, Object[] args){
        Integer methodIdInt = remoteObj.methodMap.get(method);
        if (methodIdInt == null)
             throw new RuntimeException("Method not implemented by remote object owner: "+method);

        boolean needReturn = method.getReturnType() != void.class;
        int objectId = remoteObj.objectId;
        short methodId = methodIdInt.shortValue();
        RemoteMethodCallMessage call = new RemoteMethodCallMessage();
        call.methodId = methodId;
        call.objectId = objectId;
        call.args = args;

        Invocation invoke = null;
        if (needReturn){
            call.invocationId = invocationIdCounter++;
            invoke = new Invocation();

            pendingInvocations.put(call.invocationId, invoke);
        }

        try{
            if (server != null){
                remoteObj.client.send(call);
            }else{
                client.send(call);
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }

        if (invoke != null){
            synchronized(invoke){
                while (!invoke.available){
                    try {
                        invoke.wait();
                    } catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                }
            }
            pendingInvocations.remove(call.invocationId);
            return invoke.retVal;
        }else{
            return null;
        }
    }

    public void messageReceived(Message message) {
        if (message instanceof RemoteObjectDefMessage){
            RemoteObjectDefMessage defMsg = (RemoteObjectDefMessage) message;

            ObjectDef[] defs = defMsg.objects;
            for (ObjectDef def : defs){
                RemoteObject remoteObject = new RemoteObject(this, message.getClient());
                remoteObject.objectId = (short)def.objectId;
                remoteObject.methodDefs = def.methodDefs;
                //remoteObjects.put(def.objectName, remoteObject);
                //remoteObjectsById.put(def.objectId, remoteObject);
            }
            
            synchronized (recieveObjectLock){
                recieveObjectLock.notifyAll();
            }
        }else if (message instanceof RemoteMethodCallMessage){
            RemoteMethodCallMessage call = (RemoteMethodCallMessage) message;
            LocalInterface localObj = localObjects.get(call.objectId);

            Object obj = localObj.theObject;
            Method method = localObj.methods[call.methodId];
            Object[] args = call.args;
            Object ret = null;
            try {
                ret = method.invoke(obj, args);
            } catch (Exception ex){
                throw new RuntimeException(ex);
            }

            if (method.getReturnType() != void.class){
                // send return value back
                RemoteMethodReturnMessage retMsg = new RemoteMethodReturnMessage();
                retMsg.invocationID = invocationIdCounter++;
                retMsg.retVal = ret;
                try {
                    if (server != null){
                        call.getClient().send(retMsg);
                    } else{
                        client.send(retMsg);
                    }
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }else if (message instanceof RemoteMethodReturnMessage){
            RemoteMethodReturnMessage retMsg = (RemoteMethodReturnMessage) message;
            Invocation invoke = pendingInvocations.get(retMsg.invocationID);
            if (invoke == null){
                throw new RuntimeException("Cannot find invocation ID: " + retMsg.invocationID);
            }

            synchronized (invoke){
                invoke.retVal = retMsg.retVal;
                invoke.available = true;
                invoke.notifyAll();
            }
        }
    }

    public void clientConnected(Client client) {
        if (localObjects.size() > 0){
            // send a object definition message
            ObjectDef[] defs = new ObjectDef[localObjects.size()];
            int i = 0;
            for (Entry<LocalInterface> entry : localObjects){
                defs[i] = makeObjectDef(entry.getValue());
                i++;
            }

            RemoteObjectDefMessage defMsg = new RemoteObjectDefMessage();
            defMsg.objects = defs;
            try {
                if (this.client != null){
                    this.client.send(defMsg);
                } else{
                    client.send(defMsg);
                }
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public void clientDisconnected(Client client) {
    }

    public void messageSent(Message message) {
    }

    public void objectReceived(Object object) {
    }

    public void objectSent(Object object) {
    }

}
