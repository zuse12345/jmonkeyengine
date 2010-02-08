package g3dtest.awt;

import com.g3d.system.AppSettings;
import com.g3d.system.G3DCanvasContext;
import com.g3d.system.G3DContext.Type;
import com.g3d.system.G3DSystem;
import com.g3d.system.SystemListener;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;

public class TestApplet extends Applet {

    private G3DCanvasContext context;
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
        G3DSystem.setLowPermissions(true);
        context = (G3DCanvasContext) G3DSystem.newContext(settings, Type.Canvas);
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
