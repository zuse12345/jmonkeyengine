package com.jme3.shader;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

public class Uniform extends ShaderVariable {

    public static enum Type {
        Float,
        Vector2,
        Vector3,
        Vector4,

        FloatArray,
        Vector2Array,
        Vector3Array,
        Vector4Array,

        Boolean,

        Matrix3,
        Matrix4,

        Matrix3Array,
        Matrix4Array,

        Int,
    }

    /**
     * Currently set value of the uniform.
     */
    protected Object value = null;
    protected FloatBuffer matrixValue = null;

    /**
     * Type of uniform
     */
    protected Type dataType;

    /**
     * Binding to a renderer value, or null if user-defined uniform
     */
    protected UniformBinding binding;

    public void write(JmeExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(dataType, "dataType", null);
        oc.write(binding, "binding", null);
        switch (dataType){
            case Boolean:
                oc.write( ((Boolean)value).booleanValue(), "valueBoolean", false );
                break;
            case Float:
                oc.write( ((Float)value).floatValue(), "valueFloat", 0);
                break;
            case FloatArray:
                oc.write( (FloatBuffer)value, "valueFloatArray", null);
                break;
            case Int:
                oc.write( ((Integer)value).intValue(), "valueInt", 0);
                break;
            case Matrix3:
                oc.write( (Matrix3f)value, "valueMatrix3", null);
                break;
            case Matrix3Array:
            case Matrix4Array:
            case Vector2Array:
                throw new UnsupportedOperationException("Come again?");
            case Matrix4:
                oc.write( (Matrix4f)value, "valueMatrix4", null);
                break;
            case Vector2:
                oc.write( (Vector2f)value, "valueVector2", null);
                break;
            case Vector3:
                oc.write( (Vector3f)value, "valueVector3", null);
                break;
            case Vector3Array:
                oc.write( (FloatBuffer)value, "valueVector3Array", null);
                break;
            case Vector4:
                oc.write( (ColorRGBA)value, "valueVector4", null);
                break;
            case Vector4Array:
                oc.write( (FloatBuffer)value, "valueVector4Array", null);
                break;
        }
    }

    public void read(JmeImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        dataType = ic.readEnum("dataType", Type.class, null);
        binding = ic.readEnum("binding", UniformBinding.class, null);
        switch (dataType){
            case Boolean:
                value = ic.readBoolean("valueBoolean", false);
                break;
            case Float:
                value = ic.readFloat("valueFloat", 0);
                break;
            case FloatArray:
                value = ic.readFloatBuffer("valueFloatArray", null);
                break;
            case Int:
                value = ic.readInt("valueInt", 0);
                break;
            case Matrix3:
                matrixValue = ic.readFloatBuffer("valueMatrix3", null);
                value = matrixValue;
                break;
            case Matrix4:
                matrixValue = ic.readFloatBuffer("valueMatrix4", null);
                value = matrixValue;
                break;
            case Vector2:
                value = ic.readSavable("valueVector2", null);
                break;
            case Vector3:
                value = ic.readSavable("valueVector3", null);
                break;
            case Vector3Array:
                value = ic.readFloatBuffer("valueVector3Array", null);
                break;
            case Vector4:
                value = ic.readSavable("valueVector4", null);
                break;
            case Vector4Array:
                value = ic.readFloatBuffer("valueVector4Array", null);
                break;
        }
    }

    public void setBinding(UniformBinding binding){
        this.binding = binding;
    }

    public UniformBinding getBinding(){
        return binding;
    }

    public Type getDataType() {
        return dataType;
    }

    public Object getValue(){
        return value;
    }

    public void setMatrix4(Matrix4f mat){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Matrix4)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        if (matrixValue == null){
            matrixValue = BufferUtils.createFloatBuffer(16);
            value = matrixValue;
        }
        mat.fillFloatBuffer(matrixValue, true);
        matrixValue.flip();
        dataType = Type.Matrix4;
        updateNeeded = true;
    }

    public void setMatrix3(Matrix3f mat){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Matrix3)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        if (matrixValue == null){
            matrixValue = BufferUtils.createFloatBuffer(9);
            value = matrixValue;
        }
        mat.fillFloatBuffer(matrixValue, true);
        matrixValue.flip();
        dataType = Type.Matrix3;
        updateNeeded = true;
    }

    public void setFloat(float val){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Float)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = new Float(val);
        dataType = Type.Float;
        updateNeeded = true;
    }

    public void setBoolean(boolean val) {
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Boolean)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = new Boolean(val);
        dataType = Type.Boolean;
        updateNeeded = true;
    }

    public void setFloatArray(float[] vals){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.FloatArray)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = BufferUtils.createFloatBuffer(vals);
        dataType = Type.FloatArray;
        updateNeeded = true;
    }

    public void setVector3Array(Vector3f[] vals){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector3Array)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = BufferUtils.createFloatBuffer(vals);
        dataType = Type.Vector3Array;
        updateNeeded = true;
    }

    public void setVector4Array(Quaternion[] vals){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector4Array)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        FloatBuffer fb = (FloatBuffer) value;
        if (fb == null){
            fb = BufferUtils.createFloatBuffer(vals.length * 4);
        }else if (fb.capacity() < vals.length){
            throw new BufferOverflowException();
        }
        fb.rewind();
        for (Quaternion q : vals){
            q.get(fb);
        }
        fb.flip();
        value = fb;

        dataType = Type.Vector4Array;
        updateNeeded = true;
    }

    public void setVector4Length(int length){
        if (location == -1)
            return;

        FloatBuffer fb = (FloatBuffer) value;
        if (fb == null || fb.capacity() < length){
            value = BufferUtils.createFloatBuffer(length * 4);
        }

        dataType = Type.Vector4Array;
        updateNeeded = true;
    }

    public void setVector4InArray(float x, float y, float z, float w, int index){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector4Array)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        FloatBuffer fb = (FloatBuffer) value;
        fb.position(index * 4);
        fb.put(x).put(y).put(z).put(w);
        fb.rewind();
        updateNeeded = true;
    }

    public void setVector2(Vector2f val){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector2)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = val.clone();
        dataType = Type.Vector2;
        updateNeeded = true;
    }

    public void setVector3(Vector3f val){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector3)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = val.clone();
        dataType = Type.Vector3;
        updateNeeded = true;
    }

    public void setVector4(float x, float y, float z, float w){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector4)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = new ColorRGBA(x, y, z, w);
        dataType = Type.Vector4;
        updateNeeded = true;
    }

    public void setVector4(Quaternion val){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector4)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = new ColorRGBA(val.getX(), val.getY(), val.getZ(), val.getW());
        dataType = Type.Vector4;
        updateNeeded = true;
    }

    public void setColor(ColorRGBA color){
        if (location == -1)
            return;

        if (dataType != null && dataType != Type.Vector4)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = color.clone();
        dataType = Type.Vector4;
        updateNeeded = true;
    }

    public void setInt(int val){
        if (location == -1)
            return;
        
        if (dataType != null && dataType != Type.Int)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = new Integer(val);
        dataType = Type.Int;
        updateNeeded = true;
    }

    public boolean isUpdateNeeded(){
        return updateNeeded;
    }

    public void clearUpdateNeeded(){
        updateNeeded = false;
    }

    public void reset(){
        location = -2;
        updateNeeded = true;
    }

}
