package g3dtest;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.material.Material.MatParamValue;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.dbg.Arrow;
import org.lwjgl.opengl.GL11;

public class TestDebugShapes extends SimpleApplication {

    public static void main(String[] args){
        TestDebugShapes app = new TestDebugShapes();
        app.start();
    }

    public void putArrow(Vector3f dir, ColorRGBA color){
        Geometry g = new Geometry("Arrow", new Arrow(dir));
        g.updateModelBound();

        Material mat = new Material(manager, "wire_color.j3md");
        mat.setColor("m_Color", color);
        g.setMaterial(mat);
        rootNode.attachChild(g);
    }

    @Override
    public void simpleInitApp() {
//        GL11.glLineWidth(5);
//        GL11.glEnable(GL11.GL_LINE_SMOOTH);
//        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glEnable(GL11.GL_ALPHA_TEST);
//        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);

        cam.setLocation(new Vector3f(2,1.5f,2));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        putArrow(Vector3f.UNIT_X, ColorRGBA.Red);
        putArrow(Vector3f.UNIT_Y, ColorRGBA.Green);
        putArrow(Vector3f.UNIT_Z, ColorRGBA.Blue);
    }

}
