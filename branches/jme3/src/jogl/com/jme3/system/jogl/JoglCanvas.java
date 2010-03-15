/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.system.jogl;

import com.jme3.system.JmeCanvasContext;
import java.awt.Canvas;
import javax.media.opengl.GLAutoDrawable;

public class JoglCanvas extends JoglAbstractDisplay implements JmeCanvasContext {

    private int width, height;

    public Type getType() {
        return Type.Canvas;
    }

    public void setTitle(String title) {
    }

    public void restart() {
    }

    public void destroy() {
    }

    public void create(){
        startGLCanvas();
    }

    public void display(GLAutoDrawable glad) {
        if (width != canvas.getWidth() || height != canvas.getHeight()){
            width = canvas.getWidth();
            height = canvas.getHeight();
            if (listener != null)
                listener.reshape(width, height);
        }

        if (animator.isAnimating() != autoFlush){
            if (autoFlush)
                animator.stop();
            else
                animator.start();
        }

        listener.update();
        renderer.onFrame();

        if (!canvas.isDisplayable()){
            destroy(); // native peer destroyed
        }
    }

    public Canvas getCanvas() {
        if (canvas == null)
            initGLCanvas();
        
        return canvas;
    }

}
