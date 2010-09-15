package com.jme3.network.serializing.serializers;

import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;

/**
 * The field serializer is the default serializer used for custom class.
 *
 * @author Lars Wesselius
 */
public class FieldSerializer extends Serializer {
    private static Map<Class, SavedField[]> savedFields = new HashMap<Class, SavedField[]>();

    public void initialize(Class clazz) {
        List<Field> fields = new ArrayList<Field>();

        Class processingClass = clazz;
        while (processingClass != Object.class && processingClass != Message.class) {
            Collections.addAll(fields, processingClass.getDeclaredFields());
            processingClass = processingClass.getSuperclass();
        }

        List<SavedField> cachedFields = new ArrayList(fields.size());
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isTransient(modifiers)) continue;
            if (Modifier.isFinal(modifiers)) continue;
            if (Modifier.isStatic(modifiers)) continue;
            if (field.isSynthetic()) continue;
            field.setAccessible(true);

            SavedField cachedField = new SavedField();
            cachedField.field = field;

            if (Modifier.isFinal(field.getType().getModifiers())) cachedField.serializer = Serializer.getSerializer(field.getType());

            cachedFields.add(cachedField);
        }

        Collections.sort(cachedFields, new Comparator<SavedField>() {
                public int compare (SavedField o1, SavedField o2) {
                        return o1.field.getName().compareTo(o2.field.getName());
                }
        });
        savedFields.put(clazz, cachedFields.toArray(new SavedField[cachedFields.size()]));

        
    }

    public <T> T readObject(ByteBuffer data, Class<T> c) throws IOException {
        SavedField[] fields = savedFields.get(c);

        T object = null;
        try {
            object = c.newInstance();
        } catch (Exception e) {
            throw new IOException(e);
        }

        for (SavedField savedField : fields) {
            Field field = savedField.field;
            Serializer serializer = savedField.serializer;
            Object value = null;

            if (serializer != null) {
                value = serializer.readObject(data, field.getType());
            } else {
                value = Serializer.readClassAndObject(data);
            }
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }
        return object;
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        SavedField[] fields = savedFields.get(object.getClass());


        for (SavedField savedField : fields) {
            Object val = null;
            try {
                val = savedField.field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Serializer serializer = savedField.serializer;

            try {
                if (serializer != null) {
                serializer.writeObject(buffer, val);
                } else {
                    Serializer.writeClassAndObject(buffer, val);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "[FieldSerializer][???] Exception occured on writing. Maybe you've forgotten to register a class, or maybe a class member does not have a serializer.");
                throw new IOException(e);
            }
        }
    }

    private final class SavedField {
        public Field field;
        public Serializer serializer;
    }
}
