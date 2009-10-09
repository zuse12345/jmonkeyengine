package com.g3d.material;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RenderState implements Cloneable {

    public static final RenderState DEFAULT = new RenderState();
    public static final RenderState NULL = new RenderState();

    public enum BlendMode {
        Off,
        Additive,
        PremultAlpha,
        AlphaAdditive,
        Alpha,
        Modulate,
        ModulateX2
    }

    public enum FaceCullMode {
        Off,
        Front,
        Back,
        FrontAndBack
    }

    static {
        NULL.cullMode = FaceCullMode.Off;
        NULL.depthTest = false;
    }

    boolean wireframe = false;
    FaceCullMode cullMode = FaceCullMode.Back;

    boolean depthWrite = true;
    boolean depthTest = true;

    boolean colorWrite = true;

    BlendMode blendMode = BlendMode.Off;
    boolean alphaTest = false;
    float alphaFallOff = 0;

    boolean offsetEnabled = false;
    float offsetFactor = 0;
    float offsetUnits = 0;

    @Override
    public RenderState clone(){
        try{
            return (RenderState) super.clone();
        }catch (CloneNotSupportedException ex){
            throw new AssertionError();
        }
    }

    public boolean isColorWrite() {
        return colorWrite;
    }

    public float getPolyOffsetFactor() {
        return offsetFactor;
    }

    public float getPolyOffsetUnits() {
        return offsetUnits;
    }

    public boolean isPolyOffset(){
        return offsetEnabled;
    }

    public float getAlphaFallOff() {
        return alphaFallOff;
    }

    public void setAlphaFallOff(float alphaFallOff) {
        this.alphaFallOff = alphaFallOff;
    }

    public boolean isAlphaTest() {
        return alphaTest;
    }

    public void setAlphaTest(boolean alphaTest) {
        this.alphaTest = alphaTest;
    }

    public FaceCullMode getFaceCullMode() {
        return cullMode;
    }

    public void setColorWrite(boolean colorWrite){
        this.colorWrite = colorWrite;
    }

    public void setPolyOffset(float factor, float units){
        offsetEnabled = true;
        offsetFactor = factor;
        offsetUnits = units;
    }

    public void setFaceCullMode(FaceCullMode cullMode) {
        this.cullMode = cullMode;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public boolean isDepthTest() {
        return depthTest;
    }

    public void setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
    }

    public boolean isDepthWrite() {
        return depthWrite;
    }

    public void setDepthWrite(boolean depthWrite) {
        this.depthWrite = depthWrite;
    }

    public boolean isWireframe() {
        return wireframe;
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

}
