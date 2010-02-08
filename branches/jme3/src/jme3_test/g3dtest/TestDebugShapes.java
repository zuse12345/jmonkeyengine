package g3dtest;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.dbg.Arrow;

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
        cam.setLocation(new Vector3f(2,1.5f,2));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        putArrow(Vector3f.UNIT_X, ColorRGBA.Red);
        putArrow(Vector3f.UNIT_Y, ColorRGBA.Green);
        putArrow(Vector3f.UNIT_Z, ColorRGBA.Blue);
    }

}
