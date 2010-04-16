package com.jme3.niftygui;

import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import de.lessvoid.nifty.render.BlendMode;
import de.lessvoid.nifty.spi.render.RenderDevice;
import de.lessvoid.nifty.spi.render.RenderFont;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.tools.Color;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class RenderDeviceJme implements RenderDevice {

    private NiftyJmeDisplay display;
    private RenderManager rm;
    private Renderer r;

    private final Quad quad = new Quad(1, 1);
    private final Geometry quadGeom = new Geometry("nifty-quad", quad);
    private final Geometry gradQuadGeom = new Geometry("nifty-gradient-quad", quad);
    private final Geometry imageQuadGeom = new Geometry("nifty-image-quad", quad);
//    private final Node imageQuadNode = new Node("nifty-image-node");
//    private final Node textNode = new Node("nifty-text-node");

    private final ColorRGBA colorRgba = new ColorRGBA();
    private final Material solidColor;
    private final Material gradientColor;
    private final Material imageColor;

    public static boolean GUI_DEBUG = false;
    private boolean clipWasSet = false;
    private BlendMode blendMode = null;

    private VertexBuffer quadDefaultTC = quad.getBuffer(Type.TexCoord);
    private VertexBuffer quadModTC = quadDefaultTC.clone();

    private Matrix4f tempMat = new Matrix4f();

    public RenderDeviceJme(NiftyJmeDisplay display){
        this.display = display;

        VertexBuffer vb = new VertexBuffer(Type.Color);
        vb.setNormalized(true);
        ByteBuffer bb = BufferUtils.createByteBuffer(4 * 4);
        vb.setupData(Usage.Stream, 4, Format.UnsignedByte, bb);
        quad.setBuffer(vb);

        quadModTC.setUsage(Usage.Stream);

        solidColor = new Material(display.getAssetManager(), "default_gui.j3md");
        solidColor.setBoolean("m_VertexColor", false);
        solidColor.setBoolean("m_EnableUbo", true);
//        quadGeom.setMaterial(solidColor);

        gradientColor = new Material(display.getAssetManager(), "default_gui.j3md");
        gradientColor.setBoolean("m_VertexColor", true);
        gradientColor.setColor("m_Color", ColorRGBA.White);
        gradientColor.setBoolean("m_EnableUbo", true);
//        gradQuadGeom.setMaterial(gradientColor);

        imageColor = new Material(display.getAssetManager(), "default_gui.j3md");
        imageColor.setBoolean("m_VertexColor", false);
        imageColor.setBoolean("m_EnableUbo", true);
//        imageQuadGeom.setMaterial(imageColor);
//        imageQuadNode.attachChild(imageQuadGeom);
    }

    public void setRenderManager(RenderManager rm){
        this.rm = rm;
        this.r = rm.getRenderer();
    }

    public RenderImage createImage(String filename, boolean linear) {
        if (GUI_DEBUG)
            System.out.println("LoadImage('"+filename+"', "+linear+")");
        
        return new RenderImageJme(filename, linear, display);
    }

    public RenderFont createFont(String filename) {
        if (GUI_DEBUG)
            System.out.println("LoadFont('"+filename+"')");
        
        return new RenderFontJme(filename, display);
    }

    public int getWidth() {
        return display.getWidth();
    }

    public int getHeight() {
        return display.getHeight();
    }

    public void clear() {
        if (GUI_DEBUG)
            System.out.println("Clear()");
    }

    public void setBlendMode(BlendMode blendMode) {
        if (this.blendMode != blendMode){
            this.blendMode = blendMode;

            if (GUI_DEBUG)
                System.out.println("BlendMode("+blendMode.name()+")");
        }
    }

    private RenderState.BlendMode convertBlend(){
        if (blendMode == null)
            return RenderState.BlendMode.Off;
        else if (blendMode == BlendMode.BLEND)
            return RenderState.BlendMode.Alpha;
        else if (blendMode == BlendMode.MULIPLY)
            return RenderState.BlendMode.Modulate;
        else
            throw new UnsupportedOperationException();
    }

    private ColorRGBA convertColor(Color color){
        if (color == null)
            colorRgba.set(ColorRGBA.White);
        else
            colorRgba.set(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        
        return colorRgba;
    }

    void renderText(String str, int x, int y, Color color, float size, BitmapText text){
        if (str.length() == 0)
            return;
//        textNode.detachAllChildren();
//        textNode.attachChild(text);

//        text.setColor(convertColor(color));
        text.getMaterial().setColor("m_Color", convertColor(color));
        text.setText(str);
        text.updateLogicalState(0);

        float width = text.getLineWidth();
        float height = text.getLineHeight();

        float x0 = x + 0.5f * width  * (1f - size);
        float y0 = y + 0.5f * height * (1f - size);

        tempMat.loadIdentity();
        tempMat.setTranslation(x0, getHeight() - y0, 0);
        tempMat.setScale(size, size, 0);

        rm.setWorldMatrix(tempMat);
        text.getMaterial().render(text, rm);

//        textNode.updateLogicalState(0);
//        textNode.updateModelBound();
//        textNode.updateGeometricState();
//
//        BoundingBox bbox = (BoundingBox) text.getWorldBound();
//        float width2 = bbox.getXExtent() * 2f;
//        float height2 = bbox.getYExtent() * 2f;

//        text.setLocalTranslation(-width/2f, -height/2f, 0);
//        textNode.setLocalTranslation(x + width/2f, getHeight() + (height/2f) - y, 0);
//        textNode.setLocalScale(size);
//        textNode.updateGeometricState();
//        display.getRenderManager().renderGeometry(text);

        if (RenderDeviceJme.GUI_DEBUG)
            System.out.println("DrawText('"+str+"', "+x+", "+y+", "+color+", "+size+")");
    }

    void renderTextureQuad(int x, int y, int w, int h,
                           int srcX, int srcY, int srcW, int srcH,
                           Color color, float scale,
                           int centerX, int centerY, Texture2D texture){

        imageColor.getAdditionalRenderState().setBlendMode(convertBlend());
        imageColor.setTexture("m_Texture", texture);
        imageColor.setColor("m_Color", convertColor(color));

        float imageWidth  = texture.getImage().getWidth();
        float imageHeight = texture.getImage().getHeight();
        FloatBuffer texCoords = (FloatBuffer) quadModTC.getData();

        float startX = srcX / imageWidth;
        float startY = srcY / imageHeight;
        float endX   = startX + (srcW / imageWidth);
        float endY   = startY + (srcH / imageHeight);

        startY = 1f - startY;
        endY   = 1f - endY;

        texCoords.rewind();
        texCoords.put(startX).put(startY);
        texCoords.put(endX)  .put(startY);
        texCoords.put(endX)  .put(endY);
        texCoords.put(startX).put(endY);
        texCoords.flip();
        quadModTC.updateData(texCoords);

        quad.clearBuffer(Type.TexCoord);
        quad.setBuffer(quadModTC);

        float x0 = centerX + (x - centerX) * scale;
        float y0 = centerY - (y - centerY) * scale;

        tempMat.loadIdentity();
        tempMat.setTranslation(x0, getHeight() - y0, 0);
        tempMat.setScale(w * scale, h * scale, 0);

        rm.setWorldMatrix(tempMat);
        imageColor.render(imageQuadGeom, rm);


//        imageQuadGeom.setLocalScale(w, h, 0);
//        imageQuadGeom.setLocalTranslation(-w/2f - centerX, -h/2f - centerY, 0);
//        imageQuadNode.setLocalTranslation(x + w/2f + centerX, getHeight() - (h/2f) - y + centerY, 0);
//        imageQuadNode.setLocalScale(scale);
//        imageQuadNode.updateGeometricState();
//        display.getRenderManager().renderGeometry(imageQuadGeom);

        if (GUI_DEBUG)
            System.out.println("DrawImageEx("+x+", "+y+", "+w+", "+h+", \n" +
                               "            "+srcX+", "+srcY+", "+srcW+", "+srcH+", \n" +
                               "            "+centerX+", "+centerY+", \n" +
                               "            "+color+", "+scale+")");
    }

    void renderTextureQuad(int x, int y, int width, int height,
                       Color color, float imageScale, Texture2D texture){

        imageColor.getAdditionalRenderState().setBlendMode(convertBlend());
        imageColor.setTexture("m_Texture", texture);
        imageColor.setColor("m_Color", convertColor(color));

        quad.clearBuffer(Type.TexCoord);
        quad.setBuffer(quadDefaultTC);

        float x0 = x + 0.5f * width  * (1f - imageScale);
        float y0 = y + 0.5f * height * (1f + imageScale);

        tempMat.loadIdentity();
        tempMat.setTranslation(x0, getHeight() - y0, 0);
        tempMat.setScale(width * imageScale, height * imageScale, 0);

        rm.setWorldMatrix(tempMat);
        imageColor.render(imageQuadGeom, rm);

//        imageQuadGeom.setLocalScale(width, height, 0);
//        imageQuadGeom.setLocalTranslation(-width/2f, -height/2f, 0);
//        imageQuadNode.setLocalTranslation(x + width/2f, getHeight() - (height/2f) - y, 0);
//        imageQuadNode.setLocalScale(imageScale);
//        imageQuadNode.updateGeometricState();
//
//        display.getRenderManager().renderGeometry(imageQuadGeom);

        if (GUI_DEBUG)
            System.out.println("DrawImage("+x+", "+y+", "+width+", "+height+", "+color+", "+imageScale+")");
    }

    public void renderQuad(int x, int y, int width, int height, Color color){
        solidColor.getAdditionalRenderState().setBlendMode(convertBlend());
        solidColor.setColor("m_Color", convertColor(color));

        tempMat.loadIdentity();
        tempMat.setTranslation(x, getHeight() - height - y, 0);
        tempMat.setScale(width, height, 0);

        rm.setWorldMatrix(tempMat);
        solidColor.render(quadGeom, rm);

        if (GUI_DEBUG)
            System.out.println("DrawRect("+x+", "+y+", "+width+", "+height+", "+color+")");
    }

    public void renderQuad(int x, int y, int width, int height,
                           Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {

        VertexBuffer colors = quad.getBuffer(Type.Color);
        ByteBuffer buf = (ByteBuffer) colors.getData();
        buf.rewind();
        buf.putInt(convertColor(bottomRight).asIntABGR());
        buf.putInt(convertColor(bottomLeft).asIntABGR());
        buf.putInt(convertColor(topLeft).asIntABGR());
        buf.putInt(convertColor(topRight).asIntABGR());
        buf.flip();
        colors.updateData(buf);

        gradientColor.getAdditionalRenderState().setBlendMode(convertBlend());

//        gradQuadGeom.setLocalTranslation(x, getHeight() - height - y, 0);
//        gradQuadGeom.setLocalScale(width, height, 0);
//        gradQuadGeom.updateGeometricState();
//        display.getRenderManager().renderGeometry(gradQuadGeom);

        tempMat.loadIdentity();
        tempMat.setTranslation(x, getHeight() - height - y, 0);
        tempMat.setScale(width, height, 0);

        rm.setWorldMatrix(tempMat);
        gradientColor.render(gradQuadGeom, rm);

        if (GUI_DEBUG)
            System.out.println("DrawRectEx("+x+", "+y+", "+width+", "+height+")");

    }

    public void enableClip(int x0, int y0, int x1, int y1){
        clipWasSet = true;
        r.setClipRect(x0, getHeight() - y1, x1 - x0, y1 - y0);

        if (GUI_DEBUG)
            System.out.println("ClipRect("+x0+", "+y0+", "+x1+", "+y1+")");
    }

    public void disableClip() {
        if (clipWasSet){
            if (GUI_DEBUG)
                System.out.println("DisableClip()");

            r.clearClipRect();
            clipWasSet = false;
        }
    }

}
