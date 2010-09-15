package com.jme3.network.serializing;

/**
 * A SerializerRegistration represents a connection between a class, and
 *  its serializer. It also includes the class ID, as a short.
 *
 * @author Lars Wesselius
 */
public final class SerializerRegistration {
    private Serializer serializer;
    private short id;
    private Class type;

    public SerializerRegistration(Serializer serializer, Class cls, short id) {
        this.serializer = serializer;
        type = cls;
        this.id = id;
    }

    /**
     * Get the serializer.
     *
     * @return The serializer.
     */
    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * Get the ID.
     *
     * @return The ID.
     */
    public short getId() {
        return id;
    }

    /**
     * Get the class type.
     *
     * @return The class type.
     */
    public Class getType() {
        return type;
    }
}
