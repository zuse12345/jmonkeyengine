package com.g3d.font;

/**
 * @author dhdd
 *
 * Defines a rectangle that can constrict a text paragraph.
 */
public class Rectangle {

    public final float x,  y,  width,  height;

    /**
     *
     * @param x the X value of the upper left corner of the rectangle
     * @param y the Y value of the upper left corner of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}