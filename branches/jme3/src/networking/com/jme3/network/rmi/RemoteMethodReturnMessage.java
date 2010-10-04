package com.jme3.network.rmi;


import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;

/**
 * Contains the return value for a remote method invocation, sent as a response
 * to a {@link RemoteMethodCallMessage} with a non-zero invocationID.
 *
 * @author Kirill Vainer.
 */
@Serializable
class RemoteMethodReturnMessage extends Message {

    public RemoteMethodReturnMessage(){
        super(true);
    }

    /**
     * Invocation ID that was set in the {@link RemoteMethodCallMessage}.
     */
    short invocationID;

    /**
     * The return value, could be null.
     */
    Object retVal;


    @Override
    public String toString(){
        return "MethodReturn[ID="+invocationID+", Value="+retVal.toString()+"]";
    }
}
