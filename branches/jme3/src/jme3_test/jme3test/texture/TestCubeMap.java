package jme3test.texture;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.post.HDRRenderer;
import com.jme3.renderer.Caps;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class TestCubeMap extends SimpleApplication {

    private Sphere sky = new Sphere(32, 32, 10f);
    private Geometry skyGeom = new Geometry("Sky", sky);
    private Texture envMap;

    private HDRRenderer hdrRender;

    public static void main(String[] args){
        TestCubeMap app = new TestCubeMap();
        app.start();
    }

    public void loadEnvMap(){
        TextureKey key;
        if (renderer.getCaps().contains(Caps.FloatTexture)){
            key = new TextureKey("stpeters_probe.hdr", true);
        }else{
            key = new TextureKey("stpeters_probe.jpg", true);
        }
        key.setGenerateMips(true);
        key.setAsCube(false);
        envMap = manager.loadTexture(key);
    }

    public Geometry createReflectiveTeapot(){
        Geometry g = (Geometry) manager.loadContent(new AssetKey("teapot.obj"));
        g.setLocalScale(5);
        g.updateModelBound();

        Material mat = new Material(manager, "cube_texture.j3md");
        mat.setTexture("m_Texture", envMap);
        mat.setBoolean("m_SphereMap", true);
        g.setMaterial(mat);

        return g;
    }

    public void initHDR(){
        hdrRender = new HDRRenderer(manager, renderer);

        hdrRender.setSamples(settings.getSamples());
        hdrRender.setExposure(0.80f);
        hdrRender.setWhiteLevel(10);
        hdrRender.setThrottle(0.25f);
        hdrRender.setMaxIterations(30);
        hdrRender.setUseFastFilter(false);

        viewPort.addProcessor(hdrRender);
    }

    public void setupSkyBox(){
        skyGeom.setQueueBucket(Bucket.Sky);
        skyGeom.updateModelBound();
        skyGeom.setCullHint(CullHint.Never);

        Material skyMat = new Material(manager, "sky.j3md");
        skyMat.setBoolean("m_SphereMap", true);
        skyMat.setTexture("m_Texture", envMap);
        skyMat.setVector3("m_NormalScale", new Vector3f(1, 1, 1));
        skyGeom.setMaterial(skyMat);

        rootNode.attachChild(skyGeom);
    }

    @Override
    public void simpleInitApp() {
        initHDR();
        loadEnvMap();
        setupSkyBox();

        rootNode.attachChild(createReflectiveTeapot());
//        rootNode.updateGeometricState();
 
//        cam.setLocation(new Vector3f(6, 6, -4));
//        cam.lookAt(rootNode.getWorldBound().getCenter(), Vector3f.UNIT_Y);
    }

}
