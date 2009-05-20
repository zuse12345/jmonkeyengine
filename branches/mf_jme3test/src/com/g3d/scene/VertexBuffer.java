package com.g3d.scene;

import com.g3d.math.FastMath;
import com.g3d.renderer.GLObject;
import com.g3d.renderer.Renderer;
import com.g3d.util.BufferUtils;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class VertexBuffer extends GLObject {

    public static enum Type {
        Position,
        Normal,
        TexCoord,
        Tangent,
        Binormal,
        MiscAttrib, // <- NOTE: Special handling for user specified attributes
        Index; // <- NOTE: Special handling by the renderer
    }

    public static enum Usage {
        Static,
        DynamicWriteOnly,
        StreamWriteOnly,
        Dynamic,
        Stream;
    }

    public static enum Format {
        // Floating point formats
        Half(ByteBuffer.class, 2),
        Float(FloatBuffer.class, 4),
        Double(DoubleBuffer.class, 8),

        // Fixed formats
        Byte(ByteBuffer.class, 1),
        UnsignedByte(ByteBuffer.class, 1),
        Short(ShortBuffer.class, 2),
        UnsignedShort(ShortBuffer.class, 2),
        Int(IntBuffer.class, 4),
        UnsignedInt(IntBuffer.class, 4);

        private Class<? extends Buffer> formatDataType;
        private int componentSize = 0;

        Format(Class<? extends Buffer> dataType, int componentSize){
            this.formatDataType = dataType;
            this.componentSize = componentSize;
        }

        public int getComponentSize(){
            return componentSize;
        }
    }

    protected int components = 0;
    protected int componentsLength = 0;
    protected Buffer data = null;
    protected Usage usage = Usage.Stream;
    protected Type bufType = Type.Position;
    protected Format format = Format.Float;

    /**
     * Creates an empty, uninitialized buffer.
     * Must call setupData() to initialize.
     */
    public VertexBuffer(Type type){
        super(GLObject.Type.VertexBuffer);
        this.bufType = type;
    }

    public Buffer getData(){
        return data;
    }

    public Usage getUsage(){
        return usage;
    }

    public void setUsage(Usage usage){
        if (id != -1)
            throw new UnsupportedOperationException("Data has already been sent. Cannot set usage.");

        this.usage = usage;
    }

    public Type getBufferType(){
        return bufType;
    }

    public Format getFormat(){
        return format;
    }

    public int getNumComponents(){
        return components;
    }

    public void setupData(Usage usage, int components, Format format, Buffer data){
        if (id != -1)
            throw new UnsupportedOperationException("Data has already been sent. Cannot setupData again.");

        this.data = data;
        this.components = components;
        this.usage = usage;
        this.format = format;
        this.componentsLength = components * format.getComponentSize();
    }

    public void convertToHalf(){
        if (id != -1)
            throw new UnsupportedOperationException("Data has already been sent.");

        if (format != Format.Float)
            throw new IllegalStateException("Format must be float!");

        int numElements = data.capacity() / components;
        format = Format.Half;
        this.componentsLength = components * format.getComponentSize();
        
        ByteBuffer halfData = BufferUtils.createByteBuffer(componentsLength * numElements);
        halfData.rewind();

        FloatBuffer floatData = (FloatBuffer) data;
        floatData.rewind();

        for (int i = 0; i < floatData.capacity(); i++){
            float f = floatData.get(i);
            short half = FastMath.convertFloatToHalf(f);
            halfData.putShort(half);
        }
        this.data = halfData;
    }

    @Override
    public void resetObject() {
//        assert this.id != -1;
        this.id = -1;
        setUpdateNeeded();
    }

    @Override
    public void deleteObject(Renderer r) {
        r.deleteBuffer(this);
    }

}
