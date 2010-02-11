package g3dtest.fx;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.plugins.ClasspathLocator;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;
import com.g3d.scene.plugins.ogre.OgreMaterialList;
import com.g3d.scene.plugins.ogre.OgreMeshKey;

public class TestBumpModel2 extends SimpleApplication {

    private float angle = 0;
    private PointLight pl;

    public static void main(String[] args){
        TestBumpModel2 app = new TestBumpModel2();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(-32.295086f, 54.80136f, 79.59805f));
        cam.setRotation(new Quaternion(0.074364014f, 0.92519957f, -0.24794696f, 0.27748522f));
        cam.update();

        manager.registerLocator("/bump/", "com.g3d.asset.plugins.ClasspathLocator", "dds", "jpg", "png");

        OgreMeshKey key = new OgreMeshKey("/bump/ShinyBall.meshxml", null);
        Spatial ball = (Spatial) manager.loadContent(key);
        Material mat = manager.loadMaterial("/bump/ShinyBall.j3m");
        mat.selectTechnique("OldGpu");
        ball.setMaterial(mat);
      

        rootNode.attachChild(ball);

        key = new OgreMeshKey("/bump/Conn.meshxml", null);
        Spatial conn = (Spatial) manager.loadContent(key);
        mat = manager.loadMaterial("/bump/Conn.j3m");
        mat.selectTechnique("OldGpu");
        conn.setMaterial(mat);
        
        rootNode.attachChild(conn);
        conn.setLocalScale(10);
        conn.setLocalTranslation(60, 0, 0);

        // sunset light
//        DirectionalLight dl;// = new DirectionalLight();
//        dl.setDirection(new Vector3f(-0.1f,-0.7f,1).normalizeLocal());
////        dl.setColor(new ColorRGBA(0.88f, 0.60f, 0.40f, 1.0f));
//        dl.setColor(ColorRGBA.DarkGray);
//        rootNode.addLight(dl);
//
//        // skylight
//        dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(-0.6f,-1,-0.6f).normalizeLocal());
//        dl.setColor(new ColorRGBA(0.4f, 0.4f, 0.8f, 1f));
////        dl.setColor(new ColorRGBA(0.20f, 0.44f, 0.88f, 1.0f));
//        rootNode.addLight(dl);
//
//        // white ambient light
//        dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(1, -0.5f,-0.1f).normalizeLocal());
//        dl.setColor(ColorRGBA.White);
//        dl.setColor(new ColorRGBA(1f, 0.80f, 1f, 1.0f));
//        rootNode.addLight(dl);

        pl = new PointLight();
        pl.setPosition(new Vector3f(0, 30, 0));
        pl.setColor(ColorRGBA.White);
        rootNode.addLight(pl);

        flyCam.setMoveSpeed(30);
    }

    public void simpleUpdate(float tpf){
        angle += tpf;
        angle %= FastMath.TWO_PI;
        pl.setPosition(new Vector3f(30 * FastMath.cos(angle),
                                    30,
                                    30 * FastMath.sin(angle)));
    }

}
