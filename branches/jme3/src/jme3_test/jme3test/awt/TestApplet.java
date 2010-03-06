package jme3test.awt;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.system.SystemListener;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;

public class TestApplet extends Applet {

    private JmeCanvasContext context;
    private Canvas canvas;

    public TestApplet(){
    }

    public static class AppletListener implements SystemListener {

        public void initialize() {
            System.out.println("initialize");
        }

        public void reshape(int width, int height) {
            System.out.println("reshape "+width+", "+height);
        }

        public void update() {
            //System.out.println("update");
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

    private void createCanvas(){
        AppSettings settings = new AppSettings(true);
        settings.setRenderer("JOGL");
        JmeSystem.setLowPermissions(true);
        context = (JmeCanvasContext) JmeSystem.newContext(settings, Type.Canvas);
        canvas = context.getCanvas();
        canvas.setSize(getWidth(), getHeight());
        add(canvas);
        context.setSystemListener(new AppletListener());
        context.create();
    }

    public final void update(Graphics g) {
        canvas.setSize(getWidth(), getHeight());
    }

    public void init(){
        createCanvas();
        System.out.println("applet:init");
    }

    public void start(){
        context.setAutoFlushFrames(true);
        System.out.println("applet:start");
    }

    public void stop(){
        context.setAutoFlushFrames(false);
        System.out.println("applet:stop");
    }

    public void destroy(){
        removeAll();
        context.destroy();
        System.out.println("applet:destroy");
    }

}
