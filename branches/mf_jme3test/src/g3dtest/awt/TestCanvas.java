package g3dtest.awt;

import com.g3d.system.AppSettings;
import com.g3d.system.G3DCanvasContext;
import com.g3d.system.G3DContext.Type;
import com.g3d.system.G3DSystem;
import com.g3d.system.SystemListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestCanvas extends JFrame implements SystemListener {

    public TestCanvas(){
        setSize(640, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        AppSettings settings = new AppSettings(true);
        G3DCanvasContext ctx = (G3DCanvasContext) G3DSystem.newContext(settings, Type.Canvas);
       
//        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(ctx.getCanvas());

        ctx.setAutoFlushFrames(true);
        ctx.setSystemListener(this);
        ctx.create();
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new TestCanvas().setVisible(true);
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
    }

    public void destroy() {
        System.out.println("destroy");
    }
    
}
