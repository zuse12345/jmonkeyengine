package com.g3d.collision;

import com.g3d.math.AbstractTriangle;
import com.g3d.math.Triangle;
import com.g3d.math.Vector3f;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;


public class TestSweepSphere extends JFrame {

    private static class ShapeCanvas extends Canvas {

        private BufferStrategy strategy;
        private long lastTime;
        private float time;
        private float tpf;

        public ShapeCanvas() {
//            setSize(new Dimension(512, 512));
            setBounds(0,0,512,512);
        }

        public void initDraw(){
            setIgnoreRepaint(true);
            requestFocus();
            createBufferStrategy(2);
            strategy = getBufferStrategy();
            lastTime = System.nanoTime();
            drawLoop();
        }

        private float computeTime(){
//            long timeNow = System.nanoTime();
//            long timeDiff = timeNow - lastTime;
//            lastTime = timeNow;
//            return (timeDiff / 1000000000f);
            return 0.01f;
        }

        private void drawTriangle(Graphics2D g2d, AbstractTriangle tri){
            g2d.drawLine((int) tri.get1().getX(),
                         (int) tri.get1().getY(),
                         (int) tri.get2().getX(),
                         (int) tri.get2().getY());

            g2d.drawLine((int) tri.get2().getX(),
                         (int) tri.get2().getY(),
                         (int) tri.get3().getX(),
                         (int) tri.get3().getY());

            g2d.drawLine((int) tri.get3().getX(),
                         (int) tri.get3().getY(),
                         (int) tri.get1().getX(),
                         (int) tri.get1().getY());
        }

        private void drawSweepSphere(Graphics2D g2d, SweepSphere ss){
            int vx = (int) ss.getVelocity().getX();
            int vy = (int) ss.getVelocity().getY();
            int x = (int) ss.getCenter().getX();
            int y = (int) ss.getCenter().getY();
            int w = (int) ss.getDimension().getX();
            int h = (int) ss.getDimension().getY();
            int hw = w / 2;
            int hh = h / 2;
            g2d.drawOval(x      - w, y      - h, w * 2, h * 2);
            g2d.drawOval(x + vx - w, y + vy - h, w * 2, h * 2);

            // find perpendicular to velocity
            Vector3f up = Vector3f.UNIT_Z;
            Vector3f perp = ss.getVelocity().normalize().cross(up);
            Vector3f perpN = perp.negate();

            perp.multLocal(ss.getDimension());
            perpN.multLocal(ss.getDimension());

            int px = (int) perp.getX();
            int py = (int) perp.getY();
            g2d.drawLine(x + px, y + py, x + vx + px, y + vy + py);
            g2d.drawLine(x - px, y - py, x + vx - px, y + vy - py);
        }

        public void drawLoop(){
            SweepSphere ss = new SweepSphere();
            ss.setCenter(new Vector3f(200, 200, 0));
            ss.setDimension(new Vector3f(40, 40, 40));
            ss.setVelocity(new Vector3f(100, 100, 0));

            SweepSphere ss2 = new SweepSphere();
            ss.setCenter(new Vector3f(200, 250, 0));
            ss.setDimension(new Vector3f(40, 40, 40));
            ss.setVelocity(new Vector3f(-100, 100, 0));

//            Triangle t = new Triangle();
            
            while(true){
                Graphics2D g2d = (Graphics2D) strategy.getDrawGraphics();
                g2d.clearRect(0, 0, getWidth(), getHeight());

                tpf = 0.1f;
                tpf = computeTime();
                time += tpf * 2;
                time %= 60;

//                t.set1(new Vector3f(50,  100,  0));
//                t.set2(new Vector3f(100,  200,  0));
//                t.set3(new Vector3f(200,  50,  0));
//                t.get1().addLocal(0, time, 0);
//                t.get2().addLocal(0, time, 0);
//                t.get3().addLocal(0, time, 0);
//                ss2.setCenter(new Vector3f(200, 200, 0).subtractLocal(time, 0, 0));

                CollisionResults r = new CollisionResults();
                ss.collideWith(ss2, r);
                if (r.size() > 0){
                    CollisionResult cr = r.getClosestCollision();

                    g2d.setColor(Color.black);
                    int x = (int) cr.getContactPoint().getX();
                    int y = (int) cr.getContactPoint().getY();
                    g2d.fillRect(x-4, y-4, 8, 8);

                    g2d.setColor(Color.blue);
                    int x2 = x + (int) (cr.getContactNormal().getX() * 16);
                    int y2 = y + (int) (cr.getContactNormal().getY() * 16);
                    g2d.drawLine(x, y, x2, y2);

                    g2d.setColor(Color.red);
                }else{
                    g2d.setColor(Color.black);
                }

                drawSweepSphere(g2d, ss2);
                drawSweepSphere(g2d, ss);
                
//                drawTriangle(g2d, t);

                g2d.dispose();
                strategy.show();
            }
        }

    }

    public TestSweepSphere() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        canvas = new ShapeCanvas();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new java.awt.CardLayout());
        getContentPane().add(canvas, "card2");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TestSweepSphere window = new TestSweepSphere();
                window.setSize(512, 512);
                window.setVisible(true);
                ((ShapeCanvas)window.canvas).initDraw();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public java.awt.Canvas canvas;
    // End of variables declaration//GEN-END:variables

}
