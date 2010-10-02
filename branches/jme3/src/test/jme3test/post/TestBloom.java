package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class TestBloom extends SimpleApplication {

    float angle;
    Spatial lightMdl;
    Spatial teapot;
    Geometry frustumMdl;
    WireFrustum frustum;

    
    private Sphere sphereMesh = new Sphere(10, 10, 100, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);

  

    public static void main(String[] args){
        TestBloom app = new TestBloom();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // put the camera in a bad position
        cam.setLocation(new Vector3f(-2.336393f, 11.91392f, -7.139601f));
        cam.setRotation(new Quaternion(0.23602544f, 0.11321983f, -0.027698677f, 0.96473104f));
        //cam.setFrustumFar(1000);

        Material mat = assetManager.loadMaterial("Common/Materials/RedColor.j3m");
        Material matSoil = new Material(assetManager,"Common/MatDefs/Misc/SolidColor.j3md");
        matSoil.setColor("m_Color", ColorRGBA.LightGray);


        teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        teapot.setLocalTranslation(0,0,10);

        teapot.setMaterial(mat);
        teapot.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(teapot);

         for (int i = 0; i < 30; i++) {
            Spatial t=teapot.deepClone();
            rootNode.attachChild(t);
            teapot.setLocalTranslation((float)Math.random()*3,(float)Math.random()*3,(i+2));

        }



        Geometry soil=new Geometry("soil", new Box(new Vector3f(0, -13, 550), 800, 10, 700));
        soil.setMaterial(matSoil);
        soil.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(soil);

        for (int i = 0; i < 30; i++) {
            Spatial t=teapot.deepClone();
            t.setLocalScale(10.0f);
            rootNode.attachChild(t);
            teapot.setLocalTranslation((float)Math.random()*300,(float)Math.random()*30,30*(i+2));
        }


        // load sky
        sphere.updateModelBound();
        sphere.setQueueBucket(Bucket.Sky);
        Material sky = new Material(assetManager, "Common/MatDefs/Misc/Sky.j3md");
        TextureKey key = new TextureKey("Textures/Sky/Bright/FullskiesBlueClear03.dds", true);
        key.setGenerateMips(true);
        key.setAsCube(true);
        Texture tex = assetManager.loadTexture(key);
        sky.setTexture("m_Texture", tex);
        sky.setVector3("m_NormalScale", Vector3f.UNIT_XYZ);
        sphere.setMaterial(sky);
        sphere.setCullHint(Spatial.CullHint.Never);

        rootNode.attachChild(sphere);

        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        fpp.addFilter(new BloomFilter(cam.getWidth(),cam.getHeight()));
        viewPort.addProcessor(fpp);

    }

    @Override
    public void simpleUpdate(float tpf){

    }

}
