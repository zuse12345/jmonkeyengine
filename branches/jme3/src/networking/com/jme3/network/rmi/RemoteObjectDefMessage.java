package com.jme3.network.rmi;



import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;

/**
 * Sent to expose RMI interfaces on the local client to other clients.
 * @author Kirill Vainer
 */
@Serializable
class RemoteObjectDefMessage extends Message {

    ObjectDef[] objects;
    
    public RemoteObjectDefMessage(){
        super(true);
    }

}
