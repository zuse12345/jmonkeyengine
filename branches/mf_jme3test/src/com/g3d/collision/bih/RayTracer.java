package com.g3d.collision.bih;

import com.g3d.collision.TrianglePickResults;
import com.g3d.math.Matrix4f;
import com.g3d.math.Plane;
import com.g3d.math.Ray;
import com.g3d.math.Triangle;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.res.ContentKey;
import com.g3d.res.ContentManager;
import com.g3d.scene.Mesh;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class RayTracer extends javax.swing.JFrame {

    private Camera cam;
    private BIHTree tree;

    public RayTracer(Camera cam, Mesh m) {
        this.cam = cam;

        System.out.println("Generating BIH tree for triangle array..");
        System.out.println("Scene contains "+m.getTriangleCount()+" triangles");
        long time = System.nanoTime();

        System.gc();
        System.gc();
        System.gc();
        System.gc();

        long mem = Runtime.getRuntime().freeMemory();

        tree = new BIHTree(m,2);
        tree.construct();

        System.gc();
        System.gc();
        System.gc();
        System.gc();

        long totalMem = mem - Runtime.getRuntime().freeMemory();

        long totalTime = System.nanoTime() - time;
        double millis = totalTime / 1000000.0;
        System.out.println("Generation took: "+millis+" milliseconds");
        System.out.println("Tree takes up "+totalMem+" bytes");
//        System.out.println("Using on demand BIH subdivision");

        System.out.println("Creating AWT frame contents..");
        initComponents();
    }

    private class Tracer extends JPanel {

        BufferedImage img;

        @Override
        public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;

            g2d.setBackground(Color.black);
            cam.resize(getWidth(), getHeight(), true);

            if (img != null){
                if (img.getWidth() != getWidth() || img.getHeight() != getHeight()){
                    img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                }
            }else{
                img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            }

            long numRays = getWidth()*getHeight();
            System.out.println("Ray tracing start. # of rays: "+numRays);

            long time = System.currentTimeMillis();

            Vector3f lightPos = new Vector3f(-10,10,-10);
            TrianglePickResults results = new TrianglePickResults();

            int packSize = 4;
            int packetsY = getHeight() / packSize;
            int packetsX = getWidth() / packSize;

//            Matrix4f inverseMat = new Matrix4f(cam.getViewProjectionMatrix());
//            inverseMat.invertLocal();
//
//            Vector3f store = new Vector3f();
//            Vector3f out = new Vector3f();
//            Ray r = new Ray();

            for (int y = 0; y < packetsY; y++){
                for (int x = 0; x < packetsX; x++){
                    for (int x2 = 0; x2 < packSize; x2++){
                        for (int y2 = 0; y2 < packSize; y2++){
                            int fx = x*packSize+x2;
                            int fy = y*packSize+y2;

                            // find loc
//                            store.set((float)fx / (float)getWidth(),
//                                      1f - (float)fy / (float)getHeight(), 0);
//                            float w = inverseMat.multProj(store, out);
//                            out.multLocal(1f / w);
//                            r.setOrigin(out);
//
//                            // find dir
//                            store.setZ(1);
//                            w = inverseMat.multProj(store, out);
//                            out.multLocal(1f / w);
//                            store.set(out).subtractLocal(r.getOrigin()).normalizeLocal();
//                            r.setDirection(store);

                            Vector3f pix = cam.getWorldCoordinates(new Vector2f(fx,getHeight()-fy), 0f);
                            Vector3f dir = cam.getWorldCoordinates(new Vector2f(fx,getHeight()-fy), 1f);
                            dir.subtractLocal(pix);
                            dir.normalizeLocal();

                            Ray r = new Ray(pix, dir);
                            tree.intersect(r, results);
                            Triangle t = results.getClosestTriangle();

                            if (t != null){
                                Color c;
                                Vector3f tNorm = t.getNormal();
                                Vector3f tPos  = t.getCenter().clone();
                                Vector3f diff  = lightPos.subtract(tPos).normalizeLocal();

                                float ndotl = tNorm.dot(diff);
                                ndotl = Math.min(ndotl, 1f);
                                if (ndotl > 0){
                                    c = new Color(ndotl, ndotl, ndotl);
                                }else{
                                    c = Color.black;
                                }

                                //g2d.setColor(c);
                                //g2d.drawLine(fx, fy, fx, fy);
                                img.setRGB(fx, fy, c.getRGB());
                                continue;
                            }

                            img.setRGB(fx, fy, Color.black.getRGB());
                            //g2d.setColor(Color.black);
                            //g2d.drawLine(fx, fy, fx, fy);
                        }
                    }
                }
            }

            long hits = BIHNode.hits;
            long misses = BIHNode.misses;
            double hitRate = ((double) hits / (double) misses) * 100.0;
            System.out.println("hits/misses: "+hitRate+"%");
            BIHNode.hits = 0;
            BIHNode.misses = 0;

            g2d.drawImage(img, null, 0, 0);

            long totalTime = System.currentTimeMillis() - time;
            double secTime = totalTime / 1000.0;

            System.out.println("Raytracing finish. Took "+secTime+" seconds");
            System.out.println("rays/sec: "+((double)numRays / secTime));
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tracer = new Tracer();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BIH Raytracer");
        getContentPane().setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        tracer.setMinimumSize(new java.awt.Dimension(0, 0));
        tracer.setPreferredSize(new java.awt.Dimension(512, 512));

        org.jdesktop.layout.GroupLayout tracerLayout = new org.jdesktop.layout.GroupLayout(tracer);
        tracer.setLayout(tracerLayout);
        tracerLayout.setHorizontalGroup(
            tracerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 512, Short.MAX_VALUE)
        );
        tracerLayout.setVerticalGroup(
            tracerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 512, Short.MAX_VALUE)
        );

        getContentPane().add(tracer);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        ContentManager man = new ContentManager(true);

        System.out.println("Loading teapot.obj..");
        final Mesh m = (Mesh) man.loadContent(new ContentKey("teapot.obj"));
        final Camera c = new Camera(1024, 768);
        c.setLocation(new Vector3f(1f, 1f, -1f));
        c.lookAt(m.getBound().getCenter(), Vector3f.UNIT_Y);
        c.setFrustumPerspective(45, 1, 1, 500);
        c.updateViewProjection();

        System.out.println("Loading AWT frame");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RayTracer(c,m).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel tracer;
    // End of variables declaration//GEN-END:variables

}
