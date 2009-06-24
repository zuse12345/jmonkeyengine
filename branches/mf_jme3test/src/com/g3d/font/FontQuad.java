package com.g3d.font;

import com.g3d.math.ColorRGBA;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class FontQuad {

    private int lineNumber;
    private int wordNumber;
    private float sizeScale;
    private BitmapCharacter bitmapChar = null;
    private char character;
    private float wordWidth;
    private float totalWidth;

    private float quadPosX;
    private float quadPosY;
    private float quadTexX;
    private float quadTexY;
    private float quadPosWidth;
    private float quadPosHeight;
    private float quadTexWidth;
    private float quadTexHeight;
    
    private ColorRGBA color;

    public FontQuad() {
    }

    public void appendPositions(FloatBuffer fb){
        // NOTE: subtracting the height here
        // because OGL's Ortho origin is at lower-left
        fb.put(quadPosX).put(quadPosY).put(0f);
        fb.put(quadPosX).put(quadPosY - quadPosHeight).put(0f);
        fb.put(quadPosX + quadPosWidth).put(quadPosY - quadPosHeight).put(0f);
        fb.put(quadPosX + quadPosWidth).put(quadPosY).put(0f);
    }

    public void appendTexCoords(FloatBuffer fb){
        // flip coords to be compatible with OGL
        float u0 = quadTexX;
        float v0 = 1f - quadTexY;
        float u1 = u0 + quadTexWidth;
        float v1 = v0 - quadTexHeight;

        // upper left
        fb.put(u0).put(v0);
        // lower left
        fb.put(u0).put(v1);
        // lower right
        fb.put(u1).put(v1);
        // upper right
        fb.put(u1).put(v0);
    }

    public void appendIndices(ShortBuffer sb, int quadIndex){
        // each quad has 4 indices
        short v0 = (short) (quadIndex * 4);
        short v1 = (short) (v0 + 1);
        short v2 = (short) (v0 + 2);
        short v3 = (short) (v0 + 3);

        sb.put(new short[]{ v0, v1, v2,
                            v0, v2, v3 });
    }
    
    public void setSize(float width, float height){
        quadPosWidth = width;
        quadPosHeight = height;
    }

    public void setPosition(float x, float y){
        quadPosX = x;
        quadPosY = y;
    }

    public void setX(float x){
        quadPosX = x;
    }

    public void setY(float y){
        quadPosY = y;
    }

    public float getX() {
        return quadPosX;
    }

    public float getY() {
        return quadPosY;
    }
    
    public void setUV(float u, float v, float uSize, float vSize){
        quadTexX = u;
        quadTexY = v;
        quadTexWidth = uSize;
        quadTexHeight = vSize;
    }

    public void setColor(ColorRGBA color){
        this.color = color;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int number) {
        lineNumber = number;
    }

    public int getWordNumber() {
        return wordNumber;
    }

    public void setWordNumber(int number) {
        wordNumber = number;
    }

    public float getSizeScale() {
        return sizeScale;
    }

    public void setSizeScale(float scale) {
        sizeScale = scale;
    }

    public BitmapCharacter getBitmapChar() {
        return bitmapChar;
    }

    public void setBitmapChar(BitmapCharacter ch) {
        bitmapChar = ch;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char m_character) {
        this.character = m_character;
    }

    public float getWordWidth() {
        return wordWidth;
    }

    public void setWordWidth(float width) {
        wordWidth = width;
    }

    public void setTotalWidth(float totalWidth) {
        this.totalWidth = totalWidth;
    }

    public float getTotalWidth() {
        return totalWidth;
    }
}