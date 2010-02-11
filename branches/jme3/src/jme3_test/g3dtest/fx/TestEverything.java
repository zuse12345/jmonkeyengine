package g3dtest.fx;

import com.g3d.animation.Model;
import com.g3d.app.SimpleApplication;
import com.g3d.asset.plugins.ClasspathLocator;
import com.g3d.light.DirectionalLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.post.HDRRenderer;
import com.g3d.renderer.Caps;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.renderer.queue.RenderQueue.ShadowMode;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.Spatial.CullHint;
import com.g3d.scene.plugins.ogre.MeshLoader;
import com.g3d.scene.shape.Box;
import com.g3d.scene.shape.Sphere;
import com.g3d.shadow.BasicShadowRenderer;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture.WrapMode;

public class TestEverything extends SimpleApplication {

    private BasicShadowRenderer bsr;
    private HDRRenderer hdrRender;
    private Vector3f lightDir = new Vector3f(-1, -1, .5f).normalizeLocal();

    public static void main(String[] args){
        TestEverything app = new TestEverything();
        app.start();
    }

    public void setupHdr(){
        hdrRender = new HDRRenderer(manager, renderer);
        hdrRender.setMaxIterations(40);
        hdrRender.setSamples(settings.getSamples());
        
        hdrRender.setWhiteLevel(3);
        hdrRender.setExposure(0.72f);
        hdrRender.setThrottle(1);

//        setPauseOnLostFocus(false);
//        new HDRConfig(hdrRender).setVisible(true);

        viewPort.addProcessor(hdrRender);
    }

    public void setupBasicShadow(){
        bsr = new BasicShadowRenderer(manager, 1024);
        bsr.setDirection(lightDir);
        viewPort.addProcessor(bsr);
    }

    public void setupSkyBox(){
        Sphere sky = new Sphere(32, 32, 10f);
        Geometry skyGeom = new Geometry("Sky", sky);
        skyGeom.setQueueBucket(Bucket.Sky);
        skyGeom.setShadowMode(ShadowMode.Off);
        skyGeom.updateModelBound();
        skyGeom.setCullHint(CullHint.Never);

        Texture envMap;
        if (renderer.getCaps().contains(Caps.FloatTexture)){
            envMap = manager.loadTexture("stpeters_probe.hdr");
        }else{
            envMap = manager.loadTexture("stpeters_probe.jpg");
        }
           
        Material skyMat = new Material(manager, "sky.j3md");
        skyMat.setBoolean("m_SphereMap", true);
        skyMat.setTexture("m_Texture", envMap);
        skyMat.setVector3("m_NormalScale", new Vector3f(-1, 1, -1));
        skyGeom.setMaterial(skyMat);

        rootNode.attachChild(skyGeom);
    }

    public void setupShinyBall(){
        manager.registerLocator("/bump/", "com.g3d.asset.plugins.ClasspathLocator", "dds", "jpg", "png");
        
        Spatial ball = MeshLoader.loadModel(manager, "/bump/ShinyBall.meshxml", null);
        Material mat = manager.loadMaterial("/bump/ShinyBall.j3m");
        mat.selectTechnique("OldGpu");
        ball.setMaterial(mat);
        
        ball.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(ball);

        //XXX: uncomment this when possible
//        manager.unregisterLocator("/bump/", "com.g3d.asset.plugins.ClasspathLocator", "dds", "jpg", "png");
    }

    public void setupLighting(){
        boolean hdr = hdrRender.isEnabled();

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(lightDir);
        if (hdr){
            dl.setColor(new ColorRGBA(3, 3, 3, 1));
        }else{
            dl.setColor(new ColorRGBA(.9f, .9f, .9f, 1));
        }
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, 0, -1).normalizeLocal());
        if (hdr){
            dl.setColor(new ColorRGBA(1, 1, 1, 1));
        }else{
            dl.setColor(new ColorRGBA(.4f, .4f, .4f, 1));
        }
        rootNode.addLight(dl);
    }

    public void setupFloor(){
        Material mat = manager.loadMaterial("rockwall.j3m");
        mat.selectTechnique("OldGpu");
        mat.getTextureParam("m_DiffuseMap").getValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_NormalMap").getValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_ParallaxMap").getValue().setWrap(WrapMode.Repeat);
        Box floor = new Box(Vector3f.ZERO, 50, 1f, 50);
        floor.scaleTextureCoordinates(new Vector2f(5, 5));
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setMaterial(mat);
        floorGeom.updateModelBound();
        floorGeom.setShadowMode(ShadowMode.Recieve);
        rootNode.attachChild(floorGeom);
    }

//    public void setupTerrain(){
//        Material mat = manager.loadMaterial("rock.j3m");
//        mat.selectTechnique("OldGpu");
//        mat.getTextureParam("m_DiffuseMap").getValue().setWrap(WrapMode.Repeat);
//        mat.getTextureParam("m_NormalMap").getValue().setWrap(WrapMode.Repeat);
//        try{
//            Geomap map = GeomapLoader.fromImage(TestEverything.class.getResource("/textures/heightmap.png"));
//            Mesh m = map.createMesh(new Vector3f(0.35f, 0.0005f, 0.35f), new Vector2f(10, 10), true);
//            Logger.getLogger(TangentBinormalGenerator.class.getName()).setLevel(Level.SEVERE);
//            TangentBinormalGenerator.generate(m);
//            Geometry t = new Geometry("Terrain", m);
//            t.setLocalTranslation(85, -15, 0);
//            t.setMaterial(mat);
//            t.updateModelBound();
//            t.setShadowMode(ShadowMode.Recieve);
//            rootNode.attachChild(t);
//        }catch (IOException ex){
//            ex.printStackTrace();
//        }
//
//    }

    public void setupRobotGuy(){
        Model model = (Model) MeshLoader.loadModel(manager, "OTO.meshxml", null);
        Material mat = manager.loadMaterial("oto_lit.j3m");
        mat.selectTechnique("OldGpu");
        model.getChild(0).setMaterial(mat);
        model.setAnimation("Walk");
        model.setLocalTranslation(30, 10.5f, 30);
        model.setLocalScale(2);
        model.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(model);
    }

    public void setupSignpost(){
        Spatial signpost = MeshLoader.loadModel(manager, "signpost.meshxml", null);
        Material mat = manager.loadMaterial("signpost.j3m");
        mat.selectTechnique("OldGpu");
        signpost.setMaterial(mat);
        signpost.rotate(0, FastMath.HALF_PI, 0);
        signpost.setLocalTranslation(12, 3.5f, 30);
        signpost.setLocalScale(4);
        signpost.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(signpost);
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(-32.295086f, 54.80136f, 79.59805f));
        cam.setRotation(new Quaternion(0.074364014f, 0.92519957f, -0.24794696f, 0.27748522f));
        cam.update();

        cam.setFrustumFar(300);
        flyCam.setMoveSpeed(30);

        rootNode.setCullHint(CullHint.Never);

        setupBasicShadow();
        setupHdr();

        setupLighting();
        setupSkyBox();

//        setupTerrain();
        setupFloor();
        setupRobotGuy();
        setupSignpost();
        setupShinyBall();

        
    }

}
