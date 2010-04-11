/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.gui.renderer;

import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.input.mouse.MouseInputEvent;
import de.lessvoid.nifty.render.BlendMode;
import de.lessvoid.nifty.spi.input.InputSystem;
import de.lessvoid.nifty.spi.render.RenderDevice;
import de.lessvoid.nifty.spi.render.RenderFont;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.tools.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JPanel;

/**
 *
 * @author normenhansen
 */
public class Java2dRenderDevice extends JPanel implements RenderDevice, MouseListener, MouseMotionListener {

    private int sWidth = 640;
    private int sHeight = 480;

    public static java.awt.Color color(Color _color) {
        java.awt.Color color = new java.awt.Color(_color.getRed(), _color.getGreen(), _color.getBlue());
        return color;
    }

    public static boolean sameColor(Color color1, Color color2) {
        if (color1.getRed() == color2.getRed()
                && color1.getGreen() == color2.getGreen()
                && color1.getBlue() == color2.getBlue()
                && color1.getAlpha() == color2.getAlpha()) {
            return true;
        }
        return false;
    }
    private BufferedImage image = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
    private BufferedImage imageBuffer = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
    private boolean clip = false;
    private int clipX, clipY, clipWidth, clipHeight;
    private long lastTime = 0;

    public Java2dRenderDevice() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(java.awt.event.ComponentEvent evt) {
                resize();
            }
        });
    }

    @Override
    public synchronized void paint(Graphics g) {
        super.paint(g);
        long currentTime = System.currentTimeMillis();
        ((Graphics2D) g).drawImage(imageBuffer, 0, 0, null);
        ((Graphics2D) g).setColor(java.awt.Color.WHITE);
        ((Graphics2D) g).drawString(1000 / (currentTime - lastTime) + " fps", 100, 100);
        lastTime = currentTime;
    }

    private synchronized void setImage(BufferedImage image) {
        Graphics2D g2d = (Graphics2D) imageBuffer.getGraphics();
        g2d.drawImage(image, 0, 0, imageBuffer.getWidth(), imageBuffer.getHeight(), null);
    }

    private synchronized void resize() {
//        imageBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public int getWidth() {
//        if(super.getWidth()<=0) return 1;
//        return super.getWidth();
        return sWidth;
    }

    @Override
    public int getHeight() {
//        if(super.getHeight()<=0) return 1;
//        return super.getHeight();
        return sHeight;
    }

    public RenderImage createImage(String filename, boolean filterLinear) {
        return new Java2dRenderImage(filename);
    }

    public RenderFont createFont(String filename) {
        return new Java2dRenderFont(filename);
    }

    public void beginFrame() {
    }

    public void endFrame() {
        setImage(image);
        repaint();
    }

    public void clear() {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
    }
    private BlendMode renderMode = BlendMode.BLEND;

    public void setBlendMode(BlendMode renderMode) {
        this.renderMode = renderMode;
    }

    public void renderQuad(int x, int y, int width, int height, Color color) {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        if (clip) {
            g2d.setClip(clipX, clipY, clipWidth, clipHeight);
        }
        g2d.setColor(color(color));
        g2d.fillRect(x, y, width, height);
    }

    public void renderQuad(int x, int y, int width, int height, Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        //TODO: 4-corner gradient fill
        GradientPaint grad = null;
        if (sameColor(topLeft, topRight) && sameColor(bottomLeft, bottomRight)) {
            grad = new GradientPaint(new Point(x, y), color(topLeft), new Point(x, y + height), color(bottomRight));
        }
        if (sameColor(topLeft, bottomLeft) && sameColor(topRight, bottomRight)) {
            grad = new GradientPaint(new Point(x, y), color(topLeft), new Point(x + width, y), color(bottomRight));
        }
        if (grad == null) {
            grad = new GradientPaint(new Point(x, y), color(topLeft), new Point(x, y + height), color(bottomRight));
        }
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        if (clip) {
            g2d.setClip(clipX, clipY, clipWidth, clipHeight);
        }
        g2d.setPaint(grad);
        g2d.fillRect(x, y, width, height);
    }

    public void renderImage(RenderImage image2, int x, int y, int width, int height, Color color, float imageScale) {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        if (clip) {
            g2d.setClip(clipX, clipY, clipWidth, clipHeight);
        }
        BufferedImage paintImage = ((Java2dRenderImage) image2).getImage();
        g2d.setColor(color(color));
        if (imageScale != 1.0f) {
            g2d.drawImage(paintImage.getScaledInstance(
                    Math.max(1, Math.round(width * imageScale)),
                    Math.max(1, Math.round(height * imageScale)),
                    Image.SCALE_FAST), x, y, Math.round(width * imageScale), Math.round(height * imageScale), null);
        } else {
            g2d.drawImage(paintImage, x, y, width, height, null);
        }
    }

    public void renderImage(RenderImage image2, int x, int y, int w, int h, int srcX, int srcY, int srcW, int srcH, Color color, float scale, int centerX, int centerY) {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        if (clip) {
            g2d.setClip(clipX, clipY, clipWidth, clipHeight);
        }
        BufferedImage paintImage = ((Java2dRenderImage) image2).getImage();
        if (scale != 1.0f) {
            Image myImage = paintImage.getScaledInstance(
                    Math.max(1, Math.round(paintImage.getWidth() * scale)),
                    Math.max(1, Math.round(paintImage.getHeight() * scale)),
                    Image.SCALE_FAST);
            g2d.drawImage(myImage, x, y, x + w, y + h, srcX, srcY, srcX + srcW, srcY + srcH, null);
        } else {
            g2d.drawImage(paintImage, x, y, x + w, y + h, srcX, srcY, srcX + srcW, srcY + srcH, null);
        }
    }

    public void renderFont(RenderFont rf, String string, int i, int i1, Color color, float f) {
        Font font = new Font(((Java2dRenderFont) rf).getFont().getName(), 0, Math.round(f * 20.0f));
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setFont(font);
        g2d.setColor(color(color));
        g2d.drawString(string, i, i1);
    }

    public void enableClip(int x0, int y0, int x1, int y1) {
        clip = true;
        clipX = x0;
        clipY = y0;
        clipWidth = x1 - x0;
        clipHeight = y1 - y0;
    }

    public void disableClip() {
        clip = false;
    }

    /*
     * INPUT SYSTEM
     */
    private ConcurrentLinkedQueue<MouseInputEvent> mouseEvents = new ConcurrentLinkedQueue<MouseInputEvent>();
    private ConcurrentLinkedQueue<KeyboardInputEvent> keyboardEvents = new ConcurrentLinkedQueue<KeyboardInputEvent>();
    private InputSystem inputSystem = new InputSystem() {

        public List<MouseInputEvent> getMouseEvents() {
            LinkedList list = new LinkedList();
            MouseInputEvent event = mouseEvents.poll();
            while (event != null) {
                list.add(event);
                event = mouseEvents.poll();
            }
            return list;
        }

        public List<KeyboardInputEvent> getKeyboardEvents() {
            LinkedList list = new LinkedList();
            KeyboardInputEvent event = keyboardEvents.poll();
            while (event != null) {
                list.add(event);
                event = keyboardEvents.poll();
            }
            return list;
        }
    };

    public InputSystem getInputSystem() {
        return inputSystem;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        MouseInputEvent event = new MouseInputEvent(e.getX(), getHeight() - e.getY(), true);
        mouseEvents.add(event);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        MouseInputEvent event = new MouseInputEvent(e.getX(), getHeight() - e.getY(), false);
        mouseEvents.add(event);
    }

    public void mouseMoved(MouseEvent e) {
        MouseInputEvent event = new MouseInputEvent(e.getX(), getHeight() - e.getY(), false);
        mouseEvents.add(event);
    }
}
