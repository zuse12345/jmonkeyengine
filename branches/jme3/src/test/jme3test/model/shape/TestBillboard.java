/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.model.shape;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author Kirusha
 */
public class TestBillboard extends SimpleApplication {

    public void simpleInitApp() {
        flyCam.setMoveSpeed(10);

        Quad q = new Quad(2, 2);
        Geometry g = new Geometry("Quad", q);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Blue);
        g.setMaterial(mat);

        Quad q2 = new Quad(1, 1);
        Geometry g3 = new Geometry("Quad2", q2);
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat2.setColor("m_Color", ColorRGBA.Yellow);
        g3.setMaterial(mat2);
        g3.setLocalTranslation(.5f, .5f, .01f);

        Box b = new Box(new Vector3f(0, 0, 3), .25f, .5f, .25f);
        Geometry g2 = new Geometry("Box", b);
        g2.setMaterial(mat);

        Node bb = new Node("billboard");
        bb.addControl(new BillboardControl());
        bb.attachChild(g);
        bb.attachChild(g3);

        rootNode.attachChild(bb);
        rootNode.attachChild(g2);
    }

    public static void main(String[] args) {
        TestBillboard app = new TestBillboard();
        app.start();
    }
}