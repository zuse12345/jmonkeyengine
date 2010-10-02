package com.jme3.network.rmi;

/**
 * Method definition is used to map methods on an RMI interface
 * to an implementation on a remote machine.
 *
 * @author Kirill Vainer
 */
class MethodDef {

    /**
     * Method name
     */
    String name;

    /**
     * Return type
     */
    Class<?> retType;

    /**
     * Parameter types
     */
    Class<?>[] paramTypes;
}
