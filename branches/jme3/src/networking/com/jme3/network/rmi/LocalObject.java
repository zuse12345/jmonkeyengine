package com.jme3.network.rmi;


import java.lang.reflect.Method;

/**
 * Describes a RMI interface on the local machine.
 *
 * @author Kirill Vainer
 */
public class LocalObject {

    /**
     * The RMI interface implementation
     */
    Object theObject;

    /**
     * Shared Object ID
     */
    short objectId;

    /**
     * Methods exposed by the RMI interface. The "methodID" is used
     * to look-up methods in this array.
     */
    Method[] methods;
}
