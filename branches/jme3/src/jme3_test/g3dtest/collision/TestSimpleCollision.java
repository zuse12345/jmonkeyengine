package g3dtest.collision;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.AssetKey;
import com.g3d.material.Material;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Quad;
import com.g3d.system.AppSettings;

/**
 * Test simple collision with a plane.
 */
public class TestSimpleCollision extends SimpleApplication {

    public static void main(String[] args){
        TestSimpleCollision app = new TestSimpleCollision();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
//        Sphere q = new Sphere(32, 32, 2);
        
        Quad q = new Quad(5, 5);
        q.createCollisionData();

        Geometry g = new Geometry("Quad Geom", q);      
        g.rotate(FastMath.HALF_PI, 0, FastMath.PI);        
        g.setMaterial( (Material) manager.loadContent(new AssetKey("jme_logo.j3m")));
        rootNode.attachChild(g);
        
        flyCam.setMotionAllowedListener(new SphereMotionAllowedListener(rootNode, new Vector3f(7f, 7f, 7f)));
    }

}