package com.g3d.texture;

import com.g3d.renderer.GLObject;
import com.g3d.renderer.Renderer;
import com.g3d.texture.Image.Format;

public class FrameBuffer extends GLObject {

    private int width = 0;
    private int height = 0;
    private int samples = 0;
    private RenderBuffer colorBuf = null;
    private RenderBuffer depthBuf = null;

    public class RenderBuffer {

        Texture tex;
        Image.Format format;
        int id = -1;
        int slot = -1;

        public Format getFormat() {
            return format;
        }

        public Texture getTexture(){
            return tex;
        }

        public int getId() {
            return id;
        }

        public void setId(int id){
            this.id = id;
        }

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }
    }

    public FrameBuffer(int width, int height, int samples){
        super(Type.FrameBuffer);
        if (width <= 0 || height <= 0)
                throw new IllegalArgumentException("FrameBuffer must have valid size.");

        this.width = width;
        this.height = height;
        this.samples = samples;
    }

    public void setDepthBuffer(Image.Format format){
        if (id != -1)
            throw new UnsupportedOperationException("FrameBuffer already initialized.");

        if (!format.isDepthFormat())
            throw new IllegalArgumentException("Depth buffer format must be depth.");
            
        depthBuf = new RenderBuffer();
        depthBuf.slot = -100; // -100 == special slot for DEPTH_BUFFER
        depthBuf.format = format;
    }

    public void setColorBuffer(Image.Format format){
        if (id != -1)
            throw new UnsupportedOperationException("FrameBuffer already initialized.");

        if (format.isDepthFormat())
            throw new IllegalArgumentException("Color buffer format must be color/luminance.");
        
        colorBuf = new RenderBuffer();
        colorBuf.slot = 0;
        colorBuf.format = format;
    }

    private void checkSetTexture(Texture tex, boolean depth){
        Image img = tex.getImage();
        if (img == null)
            throw new IllegalArgumentException("Texture not initialized with RTT.");

        if (depth && !img.getFormat().isDepthFormat())
            throw new IllegalArgumentException("Texture image format must be depth.");
        else if (!depth && img.getFormat().isDepthFormat())
            throw new IllegalArgumentException("Texture image format must be color/luminance.");

        // check that resolution matches texture resolution
        if (width != img.getWidth() || height != img.getHeight())
            throw new IllegalArgumentException("Texture image resolution " +
                                               "must match FB resolution");

    }

    public void setColorTexture(Texture2D tex) {
        if (id != -1)
            throw new UnsupportedOperationException("FrameBuffer already initialized.");

        Image img = tex.getImage();
        checkSetTexture(tex, false);

        colorBuf = new RenderBuffer();
        colorBuf.slot = 0;
        colorBuf.tex = tex;
        colorBuf.format = img.getFormat();
    }

    public void setDepthTexture(Texture2D tex){
        if (id != -1)
            throw new UnsupportedOperationException("FrameBuffer already initialized.");

        Image img = tex.getImage();
        checkSetTexture(tex, true);
        
        depthBuf = new RenderBuffer();
        depthBuf.slot = -100; // indicates GL_DEPTH_ATTACHMENT
        depthBuf.tex = tex;
        depthBuf.format = img.getFormat();
    }

    public RenderBuffer getColorBuffer() {
        return colorBuf;
    }

    public RenderBuffer getDepthBuffer() {
        return depthBuf;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getSamples() {
        return samples;
    }

    @Override
    public void resetObject() {
        this.id = -1;
        this.updateNeeded = true;
    }

    @Override
    public void deleteObject(Renderer r) {
        r.deleteFrameBuffer(this);
    }
}
