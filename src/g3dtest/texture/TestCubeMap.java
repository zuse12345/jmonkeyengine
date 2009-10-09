package g3dtest.texture;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.post.HDRRenderer;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.shape.Sphere;
import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import org.lwjgl.opengl.GLContext;

public class TestCubeMap extends SimpleApplication {

    private Sphere sky = new Sphere(32, 32, 10f);
    private Geometry skyGeom = new Geometry("Sky", sky);
    private Texture envMap;
    private Node hdrNode = new Node("HDR Node");

    private float lastTpf;
    private HDRRenderer hdrRender;

    public static void main(String[] args){
        TestCubeMap app = new TestCubeMap();
        app.start();
    }

    public void loadEnvMap(){ 
        envMap = manager.loadTexture("stpeters_probe.hdr");
    }

    public Geometry createReflectiveTeapot(){
//        Torus t = new Torus(32, 32, 1f, 2f);
        Geometry g = (Geometry) manager.loadModel("teapot.obj");
        g.setLocalScale(5);
        g.updateModelBound();

        Material mat = new Material(manager, "cube_texture.j3md");
        mat.setTexture("m_Texture", envMap);
        mat.setBoolean("m_SphereMap", true);
        g.setMaterial(mat);

        return g;
    }

    public void initHDR(Format bufFormat){
        hdrRender = new HDRRenderer(manager);

        hdrRender.setExposure(0.80f);
        hdrRender.setWhiteLevel(10);
        hdrRender.setThrottle(0.25f);
        hdrRender.setMaxIterations(30);
        hdrRender.setUseFastFilter(false);
        hdrRender.setBufferFormat(bufFormat);

        hdrRender.loadInitial();
        hdrRender.load(renderer, settings.getWidth(), settings.getHeight(), 0);
    }

    @Override
    public void simpleInitApp() {
        initHDR(Format.RGB111110F);
        loadEnvMap();

        skyGeom.updateModelBound();
        skyGeom.setQueueBucket(Bucket.Sky);
        Material skyMat = new Material(manager, "sky.j3md");
        skyMat.setBoolean("m_SphereMap", true);
        skyMat.setTexture("m_Texture", envMap);
        skyGeom.setMaterial(skyMat);

        hdrNode.attachChild(createReflectiveTeapot());
        hdrNode.attachChild(skyGeom);
        hdrNode.updateGeometricState();

        cam.setLocation(new Vector3f(6, 6, -4));
        cam.lookAt(hdrNode.getWorldBound().getCenter(), Vector3f.UNIT_Y);
    }

    @Override
    public void simpleUpdate(float tpf){
        hdrNode.updateLogicalState(tpf);
        hdrNode.updateGeometricState();
        lastTpf = tpf;
    }

    @Override
    public void simpleRender(Renderer r){
        hdrRender.update(lastTpf, this, hdrNode);
        render(guiNode, r);
        r.renderQueue();
    }

}
