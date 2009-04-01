package com.g3d.shader;

import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix3f;
import com.g3d.math.Matrix4f;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Renderer;
import com.g3d.util.BufferUtils;
import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

public class Uniform {

    public static enum Type {
        Float,
        Float2,
        Float3,
        Float4,

        FloatArray,
        Float2Array,
        Float3Array,
        Float4Array,

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
     * Shader who owns this uniform .
     */
    //protected Shader owner;

    protected int location = -1;

    /**
     * Name of the uniform as was declared in the shader.
     * E.g name = "g_WorldMatrix" if the decleration was
     * "uniform mat4 g_WorldMatrix;".
     */
    protected String name = null;

    /**
     * Currently set value of the uniform.
     */
    protected Object value = null;
    protected FloatBuffer matrixValue = null;

    /**
     * Type of uniform
     */
    protected Type dataType;

    public void setLocation(int location){
        this.location = location;
    }

    public int getLocation(){
        return location;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public Type getDataType() {
        return dataType;
    }

    public Object getValue(){
        return value;
    }

    public void deleteObject(Renderer r){
        // done automatically 
    }

    public void setMatrix4(Matrix4f mat){
        if (dataType != null && dataType != Type.Matrix4)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        if (matrixValue == null){
            matrixValue = BufferUtils.createFloatBuffer(16);
            value = matrixValue;
        }
        mat.fillFloatBuffer(matrixValue, true);
        matrixValue.flip();
        dataType = Type.Matrix4;
    }

    public void setMatrix3(Matrix3f mat){
        if (dataType != null && dataType != Type.Matrix3)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        if (matrixValue == null){
            matrixValue = BufferUtils.createFloatBuffer(9);
            value = matrixValue;
        }
        mat.fillFloatBuffer(matrixValue);
        matrixValue.flip();
        dataType = Type.Matrix3;
        // need to do transpose here!
    }

    public void setFloat(float val){
        if (dataType != null && dataType != Type.Float)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = new Float(val);
        dataType = Type.Float;
    }

    public void setFloatArray(float[] vals){
        if (dataType != null && dataType != Type.FloatArray)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = BufferUtils.createFloatBuffer(vals);
        dataType = Type.FloatArray;
    }

    public void setVector3Array(Vector3f[] vals){
        if (dataType != null && dataType != Type.Float3Array)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = BufferUtils.createFloatBuffer(vals);
        dataType = Type.Float3Array;
    }

    public void setVector4Array(Quaternion[] vals){
        if (dataType != null && dataType != Type.Float4Array)
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

        dataType = Type.Float3Array;
    }

    public void setVector4Length(int length){
        FloatBuffer fb = (FloatBuffer) value;
        if (fb == null || fb.capacity() < length){
            value = BufferUtils.createFloatBuffer(length * 4);
        }

        dataType = Type.Float4Array;
    }

    public void setVector4InArray(float x, float y, float z, float w, int index){
        if (dataType != null && dataType != Type.Float4Array)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        FloatBuffer fb = (FloatBuffer) value;
        fb.position(index * 4);
        fb.put(x).put(y).put(z).put(w);
        fb.rewind();
    }

    public void setVector2(Vector2f val){
        if (dataType != null && dataType != Type.Float2)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = val.clone();
        dataType = Type.Float2;
    }

    public void setVector3f(Vector3f val){
        if (dataType != null && dataType != Type.Float3)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = val.clone();
        dataType = Type.Float3;
    }

    public void setColor(ColorRGBA color){
        if (dataType != null && dataType != Type.Float4)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = color.clone();
        dataType = Type.Float4;
    }

    public void setInt(int val){
        if (dataType != null && dataType != Type.Int)
            throw new IllegalArgumentException("Expected a "+dataType.name()+" value!");

        value = new Integer(val);
        dataType = Type.Int;
    }

}
