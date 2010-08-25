package jme3test.water;

import jme3test.scene.*;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.water.SimpleWaterProcessor;
import java.io.File;

public class TestSceneWater extends SimpleApplication {

    private Sphere sphereMesh = new Sphere(32, 32, 10, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);
    private static boolean useHttp = false;

    public static void main(String[] args) {
        File file = new File("wildhouse.zip");
        if (!file.exists()) {
            useHttp = true;
        }
        TestSceneWater app = new TestSceneWater();
        app.start();
    }

    @Override
    public void simpleUpdate(float tpf){
        sphere.setLocalTranslation(cam.getLocation());
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(10);
        Node mainScene=new Node();
        cam.setLocation(new Vector3f(-27.0f, 4.0f, 75.0f));
        cam.setRotation(new Quaternion(0.03f, 0.9f, 0f, 0.4f));

        // load sky
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
        mainScene.attachChild(sphere);

        // create the geometry and attach it
        // load the level from zip or http zip
        if (useHttp) {
            assetManager.registerLocator("http://jmonkeyengine.googlecode.com/files/wildhouse.zip", HttpZipLocator.class.getName());
        } else {
            assetManager.registerLocator("wildhouse.zip", ZipLocator.class.getName());
        }
        Spatial scene = assetManager.loadModel("main.scene");

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.4790551f, -0.39247334f, -0.7851566f));
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        scene.addLight(sun);


        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(mainScene);


        Geometry water=new Geometry("water", new Quad(400,400));
        water.updateGeometricState();
        water.updateModelBound();
        water.setShadowMode(ShadowMode.Recieve);


        Material mat = assetManager.loadMaterial("Common/Materials/WhiteColor.j3m");
       

        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        water.setMaterial(waterProcessor.getMaterial());
        water.setLocalTranslation(-200, -6, 250);

        rootNode.attachChild(water);

        viewPort.addProcessor(waterProcessor);

        mainScene.attachChild(scene);
        rootNode.attachChild(mainScene);
    }
}
