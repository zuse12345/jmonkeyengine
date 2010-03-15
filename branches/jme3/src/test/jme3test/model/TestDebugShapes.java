package jme3test.model;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Arrow;

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
