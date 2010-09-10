package jme3test.post;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.post.filters.RadialBlurFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.texture.Texture;

public class TestLightScattering extends SimpleApplication {



   
 private Sphere sphereMesh = new Sphere(10, 10, 100, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);

    public static void main(String[] args){
        TestLightScattering app = new TestLightScattering();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // put the camera in a bad position
//        cam.setLocation(new Vector3f(-2.336393f, 11.91392f, -7.139601f));
//        cam.setRotation(new Quaternion(0.23602544f, 0.11321983f, -0.027698677f, 0.96473104f));
//        cam.setDirection(new Vector3f(0,-0.5f,1.0f));
//        cam.setLocation(new Vector3f(0, 300, -500));
        //cam.setFrustumFar(1000);
flyCam.setMoveSpeed(10);
        Material mat = assetManager.loadMaterial("Textures/Terrain/Rocky/Rocky.j3m");
        Spatial scene= assetManager.loadModel("Models/Terrain/Terrain.mesh.xml");
        scene.setMaterial(mat);
        scene.setShadowMode(ShadowMode.CastAndRecieve);
        scene.setLocalScale(400);
        scene.setLocalTranslation(0, -10, -120);

        rootNode.attachChild(scene);
        
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
      
        rootNode.attachChild(sphere);

        DirectionalLight sun = new DirectionalLight();
        Vector3f lightDir=new Vector3f(-0.12f, -0.3729129f, 0.74847335f);
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        scene.addLight(sun);

        PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager,1024,4);
        pssmRenderer.setDirection(lightDir);
        pssmRenderer.setShadowIntensity(0.3f);
        viewPort.addProcessor(pssmRenderer);

        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
//        SSAOFilter ssaoFilter= new SSAOFilter(viewPort, new SSAOConfig(0.36f,1.8f,0.84f,0.16f,false,true));
//        fpp.addFilter(ssaoFilter);


           Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat2.setTexture("m_ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));

        Sphere lite=new Sphere(8, 8, 10.0f);
        Geometry lightSphere=new Geometry("lightsphere", lite);
        lightSphere.setMaterial(mat2);
        Vector3f lightPos=lightDir.multLocal(-3000);
        lightSphere.setLocalTranslation(lightPos);
       // rootNode.attachChild(lightSphere);
       LightScatteringFilter filter=new LightScatteringFilter(lightPos);
       filter.setNbSamples(80);
       filter.setBlurStart(0.02f);
       LightScatteringUI ui=new LightScatteringUI(inputManager, filter);
        fpp.addFilter(filter);

       //fpp.addFilter(new RadialBlurFilter(0.3f,15.0f));
    //    SSAOUI ui=new SSAOUI(inputManager, ssaoFilter.getConfig());

        viewPort.addProcessor(fpp);
    }

    @Override
    public void simpleUpdate(float tpf){

    }

}
