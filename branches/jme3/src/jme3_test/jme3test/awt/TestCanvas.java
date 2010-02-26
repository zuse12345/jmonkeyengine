package jme3test.awt;

import com.jme3.system.AppSettings;
import com.jme3.system.G3DCanvasContext;
import com.jme3.system.G3DContext.Type;
import com.jme3.system.G3DSystem;
import com.jme3.system.SystemListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestCanvas extends JFrame implements SystemListener {

    public TestCanvas(){
        setSize(640, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        AppSettings settings = new AppSettings(true);
        G3DCanvasContext ctx = (G3DCanvasContext) G3DSystem.newContext(settings, Type.Canvas);
        getContentPane().add(ctx.getCanvas());

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
