/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.water;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.water.SimpleWaterProcessor;

/**
 *
 * @author normenhansen
 */
public class TestSimpleWater extends SimpleApplication {

    public static void main(String[] args) {
        TestSimpleWater app = new TestSimpleWater();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(rootNode);

        Quad quad = new Quad(10, 10);
        Geometry waterPlane = new Geometry("WaterPlane", quad);
//        waterPlane.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        waterPlane.setMaterial(waterProcessor.getMaterial());
//        waterPlane.setLocalTranslation(-5, 0, 5);

        rootNode.attachChild(waterPlane);

        viewPort.addProcessor(waterProcessor);
    }
}
