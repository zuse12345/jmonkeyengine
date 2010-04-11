package jme3test.awt;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;
import java.awt.Canvas;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestCanvas extends JFrame implements SystemListener {

    private Timer timer;
    private JmeCanvasContext ctx;
    private Canvas canvas;
    private float time = 0;

    public TestCanvas(){
        setSize(640, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        AppSettings settings = new AppSettings(true);
//        settings.setRenderer("JOGL");
        ctx = (JmeCanvasContext) JmeSystem.newContext(settings, Type.Canvas);
        canvas = ctx.getCanvas();
        getContentPane().add(canvas);

        // SET IT VISIBLE
        setVisible(true);
        // NOW IT IS POSSIBLE TO BIND THE OPENGL TO THE CANVAS

        ctx.setAutoFlushFrames(true);
        ctx.setSystemListener(this);
        ctx.create();
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new TestCanvas();
            }
        });
    }

    public void initialize() {
        System.out.println("initialize");
        timer = ctx.getTimer();
        timer.update();
    }

    public void reshape(int width, int height) {
        System.out.println("reshape "+width+", "+height);
    }

    public void update() {
        if (time >= 0){
            timer.update();
            time += timer.getTimePerFrame();
            if (time > 2){
                System.out.println("Removing canvas from frame!");
                time = -1;
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        getContentPane().remove(canvas);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                        getContentPane().add(canvas);
                        System.out.println("Restored canvas!");
                    }
                });
            }
        }
    }

    public void requestClose(boolean esc) {
        System.out.println("closeRequest");
    }

    public void gainFocus() {
        System.out.println("gainfocus");
    }

    public void loseFocus() {
        System.out.println("losefocus");
    }

    public void handleError(String errorMsg, Throwable t) {
        System.out.println(errorMsg);
        t.printStackTrace();
    }

    public void destroy() {
        System.out.println("destroy");
    }
    
}
