package com.g3d.shader;

import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix3f;
import com.g3d.math.Matrix4f;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.util.BufferUtils;
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

        Matrix2,
        Matrix3,
        Matrix4,

        Matrix2Array,
        Matrix3Array,
        Matrix4Array,

        Int,
        Int2,
        Int3,
        Int4
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

        dataType = Type.Vector3Array;
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

}
