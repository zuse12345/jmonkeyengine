package com.jme3.network.serializing.serializers;

import com.jme3.network.serializing.Serializer;
import com.jme3.network.serializing.SerializerRegistration;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * Serializes collections.
 *
 * @author Lars Wesselius
 */
public class CollectionSerializer extends Serializer {

    public <T> T readObject(ByteBuffer data, Class<T> c) throws IOException {
        int length = data.getInt();

        Collection collection = null;
        try {
            collection = (Collection)c.newInstance();
        } catch (Exception e) {
            log.log(Level.WARNING, "[Serializer][???] Could not determine collection type. Using ArrayList.");
            collection = new ArrayList(length);
        }

        if (length == 0) return (T)collection;

        if (data.get() == (byte)1) {
            SerializerRegistration reg = Serializer.readClass(data);
            Class clazz = reg.getType();
            Serializer serializer = reg.getSerializer();

            for (int i = 0; i != length; ++i) {
                collection.add(serializer.readObject(data, clazz));
            }
        } else {
            for (int i = 0; i != length; ++i) {
                collection.add(Serializer.readClassAndObject(data));
            }
        }
        return (T)collection;
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        Collection collection = (Collection)object;
        int length = collection.size();

        buffer.putInt(length);
        if (length == 0) return;

        Iterator it = collection.iterator();
        Class elementClass = it.next().getClass();
        while (it.hasNext()) {
            Object obj = it.next();

            if (obj.getClass() != elementClass) {
                elementClass = null;
                break;
            }
        }

        if (elementClass != null) {
            buffer.put((byte)1);
            Serializer.writeClass(buffer, elementClass);
            Serializer serializer = Serializer.getSerializer(elementClass);

            for (Object elem : collection) {
                serializer.writeObject(buffer, elem);
            }
        } else {
            buffer.put((byte)0);
            for (Object elem : collection) {
                Serializer.writeClassAndObject(buffer, elem);
            }
        }
    }
}
