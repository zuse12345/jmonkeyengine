package com.jme3.network.rmi;


import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;

/**
 * Sent to a remote client to make a remote method invocation.
 *
 * @author Kirill Vainer
 */
@Serializable
public class RemoteMethodCallMessage extends Message {

    /**
     * The object ID on which the call is being made.
     */
    short objectId;

    /**
     * The method ID used for look-up in the LocalObject.methods array.
     */
    short methodId;

    /**
     * Invocation ID is used to identify a particular call if the calling
     * client needs the return value of the called RMI method.
     * This is set to zero if the method does not return a value.
     */
    short invocationId;

    /**
     * Arguments of the remote method invocation.
     */
    Object[] args;

    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("MethodCall[objectID=").append(objectId).append(", methodID=")
          .append(methodId);
        if (args != null && args.length > 0){
            sb.append(", args={");
            for (Object arg : args){
                sb.append(arg.toString()).append(", ");
            }
            sb.setLength(sb.length()-2);
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
