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
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author normenhansen
 */
public class Java2dRenderDevice extends JPanel implements RenderDevice, MouseListener {

    public static java.awt.Color color(Color _color) {
        java.awt.Color color = new java.awt.Color(_color.getRed(), _color.getGreen(), _color.getBlue());
        return color;
    }
    private BufferedImage image = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);
    private BufferedImage imageBuffer = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);

    @Override
    public synchronized void paint(Graphics g) {
        super.paint(g);
        addMouseListener(this);
        ((Graphics2D) g).drawImage(imageBuffer, 0, 0, null);
        ((Graphics2D) g).setColor(java.awt.Color.WHITE);
        ((Graphics2D) g).drawString(System.currentTimeMillis() + "", 100, 100);
    }

    private synchronized void setImage(BufferedImage image) {
        Graphics2D g2d = (Graphics2D) imageBuffer.getGraphics();
        g2d.drawImage(image, 0, 0, null);
    }

    @Override
    public int getWidth() {
        return 1024;
//        return super.getWidth();
    }

    @Override
    public int getHeight() {
        return 768;
//        return super.getHeight();
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
//        Graphics2D g2d = (Graphics2D) image.getGraphics();
//        for (int yPos = 0; yPos < height; yPos++) {
//            for (int xPos = 0; xPos < width; xPos++) {
//                float dist_a=(float)Math.sqrt(Math.pow(xPos,2)+Math.pow(yPos,2));
//                float dist_b=(float)Math.sqrt(Math.pow(width-xPos,2)+Math.pow(yPos,2));
//                float dist_c=(float)Math.sqrt(Math.pow(xPos,2)+Math.pow(height-yPos,2));
//                float dist_d=(float)Math.sqrt(Math.pow(width-xPos,2)+Math.pow(height-yPos,2));
//                float sum_dist=dist_a+dist_b+dist_c;
//                float r=((topLeft.getRed()*dist_a)+
//                        (topRight.getRed()*dist_b)+
//                        (bottomLeft.getRed()*dist_c)+
//                        (bottomRight.getRed()*dist_d))/sum_dist;
//                float g=((topLeft.getGreen()*dist_a)+
//                        (topRight.getGreen()*dist_b)+
//                        (bottomLeft.getGreen()*dist_c)+
//                        (bottomRight.getGreen()*dist_d))/sum_dist;
//                float b=((topLeft.getBlue()*dist_a)+
//                        (topRight.getBlue()*dist_b)+
//                        (bottomLeft.getBlue()*dist_c)+
//                        (bottomRight.getBlue()*dist_d))/sum_dist;
//                r=Math.min(1, r);
//                g=Math.min(1, g);
//                b=Math.min(1, b);
//                g2d.setColor(new java.awt.Color(r,g,b));
//                g2d.drawRect(x+xPos, y+yPos, 1, 1);
//            }
//        }
        //TODO: 4-corner gradient fill
        GradientPaint grad = new GradientPaint(new Point(x, y), color(topLeft), new Point(x, y + height), color(bottomRight));
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
        Font font=new Font(((Java2dRenderFont)rf).getFont().getName(),0,Math.round(f*20));
        Graphics2D g2d=(Graphics2D)image.getGraphics();
        g2d.setFont(font);
        g2d.setColor(color(color));
        g2d.drawString(string, i, i1);
    }

    private boolean clip = false;
    private int clipX, clipY, clipWidth, clipHeight;

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

    /**
     * @return the inputSystem
     */
    public InputSystem getInputSystem() {
        return inputSystem;
    }
    int lastX = 0;
    int lastY = 0;

    public void mouseClicked(MouseEvent e) {
        //TODO: crude way to avoid multiple calls (due to repaint?)
        if (lastX == e.getX() && lastY == e.getY()) {
            return;
        }
        MouseInputEvent event = new MouseInputEvent(e.getX(), getHeight() - e.getY(), true);
        mouseEvents.add(event);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "mousee:" + mouseEvents.size());
        lastX = e.getX();
        lastY = e.getY();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

}
