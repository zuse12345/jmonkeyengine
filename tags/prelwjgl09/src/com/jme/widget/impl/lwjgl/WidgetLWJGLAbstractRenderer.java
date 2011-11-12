/* 
* Copyright (c) 2004, jMonkeyEngine - Mojo Monkey Coding 
* All rights reserved. 
* 
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions are met: 
* 
* Redistributions of source code must retain the above copyright notice, this 
* list of conditions and the following disclaimer. 
* 
* Redistributions in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation 
* and/or other materials provided with the distribution. 
* 
* Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the 
* names of its contributors may be used to endorse or promote products derived 
* from this software without specific prior written permission. 
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
* POSSIBILITY OF SUCH DAMAGE. 
* 
*/
package com.jme.widget.impl.lwjgl;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLU;

import com.jme.renderer.ColorRGBA;
import com.jme.widget.Widget;
import com.jme.widget.WidgetTextureCoords;
import com.jme.widget.border.WidgetBorder;
import com.jme.widget.border.WidgetBorderType;
import com.jme.widget.bounds.WidgetViewRectangle;
import com.jme.widget.renderer.WidgetAbstractRenderer;

/**
 * <code>WidgetLWJGLAbstractRenderer</code>
 * @author Gregg Patton
 * @version $Id: WidgetLWJGLAbstractRenderer.java,v 1.1 2004-03-04 03:28:41 greggpatton Exp $
 */
public abstract class WidgetLWJGLAbstractRenderer extends WidgetAbstractRenderer {

    public WidgetLWJGLAbstractRenderer(Widget w) {
        super(w);
    }

    protected void drawBox2d(Widget w) {
        initWidgetProjection(w);

        int l = w.getX() + w.getXOffset();
        int b = w.getY() + w.getYOffset();

        int r = l + w.getWidth();
        int t = b + w.getHeight();

        drawBox2d(t, l, b, r, w.getBorder(), w.getBgColor(), w.getTextureCoords());

        resetWidgetProjection();
    }

    protected void drawBoxBorder2d(Widget w) {

        WidgetBorder border = w.getBorder();

        if (border != null) {
            if (border.getType() == WidgetBorderType.RAISED) {
                drawRaisedBoxBorder2d(w);
            } else if (border.getType() == WidgetBorderType.LOWERED) {
                drawLoweredBoxBorder2d(w);
            } else if (border.getType() == WidgetBorderType.FLAT) {
                drawFlatBoxBorder2d(w);
            }
        }

    }

    protected void drawFlatBoxBorder2d(Widget w) {

        initWidgetProjection(w);

        int l = w.getX() + w.getXOffset();
        int b = w.getY() + w.getYOffset();

        int r = l + w.getWidth();
        int t = b + w.getHeight();

        drawFlatBoxBorder2d(t, l, b, r, w.getBorder());

        resetWidgetProjection();
    }

    protected void drawLoweredBoxBorder2d(Widget w) {
        initWidgetProjection(w);

        int l = w.getX() + w.getXOffset();
        int b = w.getY() + w.getYOffset();

        int r = l + w.getWidth();
        int t = b + w.getHeight();

        drawLoweredBoxBorder2d(t, l, b, r, w.getBorder());

        resetWidgetProjection();
    }

    protected void drawRaisedBoxBorder2d(Widget w) {
        initWidgetProjection(w);

        int l = w.getX() + w.getXOffset();
        int b = w.getY() + w.getYOffset();

        int r = l + w.getWidth();
        int t = b + w.getHeight();

        drawRaisedBoxBorder2d(t, l, b, r, w.getBorder());

        resetWidgetProjection();
    }

    protected void drawBox2d(
        int top,
        int left,
        int bottom,
        int right,
        WidgetBorder border,
        ColorRGBA color) {

        drawBox2d(top, left, bottom, right, border, color, null);
    }

    protected void drawBox2d(
        int top,
        int left,
        int bottom,
        int right,
        WidgetBorder border,
        ColorRGBA color,
        WidgetTextureCoords tc) {

        int l = left;
        int b = bottom;
        int r = right;
        int t = top;

        if (border != null) {
            l += border.left;
            b += border.bottom;
            r -= border.right;
            t -= border.top;
        }

        if (tc != null) {

            alphaState.set();

            GL.glBegin(GL.GL_QUADS);

            GL.glTexCoord2f(tc.u0, tc.v0);
            GL.glVertex2f(l, b);

            GL.glTexCoord2f(tc.u1, tc.v0);
            GL.glVertex2f(r, b);

            GL.glTexCoord2f(tc.u1, tc.v1);
            GL.glVertex2f(r, t);

            GL.glTexCoord2f(tc.u0, tc.v1);
            GL.glVertex2f(l, t);

            GL.glEnd();

            alphaState.unset();

        } else if (color != null) {

            GL.glColor3f(color.r, color.g, color.b);

            GL.glBegin(GL.GL_QUADS);

            GL.glVertex2f(l, b);

            GL.glVertex2f(r, b);

            GL.glVertex2f(r, t);

            GL.glVertex2f(l, t);

            GL.glEnd();
        }

    }

    protected void drawBoxBorder2d(
        int top,
        int left,
        int bottom,
        int right,
        WidgetBorder border,
        ColorRGBA topLeft,
        ColorRGBA bottomRight) {

        GL.glBegin(GL.GL_QUADS);

        GL.glColor3f(topLeft.r, topLeft.g, topLeft.b);

        GL.glVertex2f(left, bottom);
        GL.glVertex2f(left + border.left, bottom + border.bottom);
        GL.glVertex2f(left + border.left, top - border.top);
        GL.glVertex2f(left, top);

        GL.glVertex2f(left, top);
        GL.glVertex2f(left + border.left, top - border.top);
        GL.glVertex2f(right - border.right, top - border.top);
        GL.glVertex2f(right, top);

        GL.glColor3f(bottomRight.r, bottomRight.g, bottomRight.b);

        GL.glVertex2f(left, bottom);
        GL.glVertex2f(right, bottom);
        GL.glVertex2f(right - border.right, bottom + border.bottom);
        GL.glVertex2f(left + border.left, bottom + border.bottom);

        GL.glVertex2f(right, bottom);
        GL.glVertex2f(right - border.right, bottom + border.bottom);
        GL.glVertex2f(right - border.right, top - border.top);
        GL.glVertex2f(right, top);

        GL.glEnd();
    }

    protected void drawFlatBoxBorder2d(int top, int left, int bottom, int right, WidgetBorder border) {
        drawBoxBorder2d(top, left, bottom, right, border, border.getFlatColor(), border.getFlatColor());
    }

    protected void drawLoweredBoxBorder2d(int top, int left, int bottom, int right, WidgetBorder border) {
        drawBoxBorder2d(top, left, bottom, right, border, border.getDarkColor(), border.getLightColor());
    }

    protected void drawRaisedBoxBorder2d(int top, int left, int bottom, int right, WidgetBorder border) {
        drawBoxBorder2d(top, left, bottom, right, border, border.getLightColor(), border.getDarkColor());
    }

    protected void initWidgetProjection(Widget widget) {
        WidgetViewRectangle v;

        Widget p = widget.getWidgetParent();

        if (p != null) {
            v = p.getViewRectangle();
        } else {
            v = widget.getViewRectangle();
        }

        int x = (int) v.getMinX();
        int y = (int) v.getMinY();
        int w = (int) v.getWidth();
        int h = (int) v.getHeight();

        //int screenWidth = DisplaySystem.getDisplaySystem().getWidth();
        //int screenHeight = DisplaySystem.getDisplaySystem().getHeight();

        //GL.glViewport(0, 0, screenWidth, screenHeight);
        GL.glViewport(x, y, w, h);

        GL.glEnable(GL.GL_SCISSOR_TEST);

        GL.glScissor(x, y, w, h);

        GL.glMatrixMode(GL.GL_PROJECTION);
        GL.glPushMatrix();

        GL.glLoadIdentity();

        //GLU.gluOrtho2D(0, screenWidth, 0, screenHeight);
        GLU.gluOrtho2D(0, w, 0, h);

        GL.glMatrixMode(GL.GL_MODELVIEW);
        GL.glPushMatrix();

        GL.glLoadIdentity();

    }

    protected void resetWidgetProjection() {
        GL.glDisable(GL.GL_SCISSOR_TEST);
        GL.glMatrixMode(GL.GL_PROJECTION);
        GL.glPopMatrix();
        GL.glMatrixMode(GL.GL_MODELVIEW);
        GL.glPopMatrix();

        updateCamera();
    }

}