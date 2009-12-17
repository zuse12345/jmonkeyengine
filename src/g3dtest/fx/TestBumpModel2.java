package g3dtest.fx;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.plugins.ClasspathLocator;
import com.g3d.light.DirectionalLight;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;
import g3dtest.export.ImpExp;

public class TestBumpModel2 extends SimpleApplication {

    public static void main(String[] args){
        TestBumpModel2 app = new TestBumpModel2();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        manager.registerLocator("/bump/", ClasspathLocator.class, "dds", "jpg", "png");

        Spatial ball = manager.loadOgreModel("/bump/ShinyBall.meshxml", null);
        ball.setMaterial(manager.loadMaterial("/bump/ShinyBall.j3m"));
        rootNode.attachChild(ball);

        Spatial conn = manager.loadOgreModel("/bump/Conn.meshxml", null);
        conn.setMaterial(manager.loadMaterial("/bump/Conn.j3m"));
        rootNode.attachChild(conn);
        conn.setLocalScale(10);
        conn.setLocalTranslation(60, 0, 0);

        // sunset light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f,-0.7f,1).normalizeLocal());
//        dl.setColor(new ColorRGBA(0.88f, 0.60f, 0.40f, 1.0f));
        dl.setColor(ColorRGBA.DarkGray);
        rootNode.addLight(dl);

        // skylight
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.6f,-1,-0.6f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.4f, 0.4f, 0.8f, 1f));
//        dl.setColor(new ColorRGBA(0.20f, 0.44f, 0.88f, 1.0f));
        rootNode.addLight(dl);

        // white ambient light
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, -0.5f,-0.1f).normalizeLocal());
        dl.setColor(ColorRGBA.White);
//        dl.setColor(new ColorRGBA(1f, 0.80f, 1f, 1.0f));
        rootNode.addLight(dl);

        cam.setLocation(new Vector3f(150, 150, 150));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(30);
    }

}
