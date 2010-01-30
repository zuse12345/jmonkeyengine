package g3dtest.texture;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.post.HDRRenderer;
import com.g3d.renderer.Caps;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial.CullHint;
import com.g3d.scene.shape.Sphere;
import com.g3d.texture.Texture;

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
        if (renderer.getCaps().contains(Caps.FloatTexture)){
            envMap = manager.loadTexture("stpeters_probe.hdr", false, true, false, 0);
        }else{
            envMap = manager.loadTexture("stpeters_probe.jpg", false, true, false, 0);
        }
    }

    public Geometry createReflectiveTeapot(){
        Geometry g = (Geometry) manager.loadModel("teapot.obj");
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
