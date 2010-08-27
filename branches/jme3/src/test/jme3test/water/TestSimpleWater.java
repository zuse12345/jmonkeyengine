/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.water;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.water.SimpleWaterProcessor;

/**
 *
 * @author normenhansen
 */
public class TestSimpleWater extends SimpleApplication implements ActionListener {
    Material mat;
    Geometry waterPlane;
    SimpleWaterProcessor waterProcessor;
    boolean useWater = true;

    public static void main(String[] args) {
        TestSimpleWater app = new TestSimpleWater();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //init input
        inputManager.addMapping("use_water", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addListener(this, "use_water");

        //init cam location
        cam.setLocation(new Vector3f(0, 10, 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        //init scene
        Node sceneNode = new Node("Scene");
        mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        geom.setMaterial(mat);
        sceneNode.attachChild(geom);

        // load sky
        Sphere sphereMesh = new Sphere(32, 32, 10, false, true);
        Geometry sphere = new Geometry("Sky", sphereMesh);
        sphere.updateModelBound();
        sphere.setQueueBucket(Bucket.Sky);
        Material sky = new Material(assetManager, "Common/MatDefs/Misc/Sky.j3md");
        TextureKey key = new TextureKey("Textures/Sky/Bright/BrightSky.dds", true);
        key.setGenerateMips(true);
        key.setAsCube(true);
        Texture tex = assetManager.loadTexture(key);
        sky.setTexture("m_Texture", tex);
        sky.setVector3("m_NormalScale", Vector3f.UNIT_XYZ);
        sphere.setMaterial(sky);
        sceneNode.attachChild(sphere);

        rootNode.attachChild(sceneNode);

        //create processor
        waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(sceneNode);
        waterProcessor.setDebug(true);
        viewPort.addProcessor(waterProcessor);

        //create water quad
        waterPlane = waterProcessor.createWaterGeometry(10, 10);
        waterPlane.setLocalTranslation(-5, 0, 5);

        rootNode.attachChild(waterPlane);
    }

    public void onAction(String name, boolean value, float tpf) {
        if (name.equals("use_water") && value) {
            if (!useWater) {
                useWater = true;
                waterPlane.setMaterial(waterProcessor.getMaterial());
            } else {
                useWater = false;
                waterPlane.setMaterial(mat);
            }
        }
    }
}
