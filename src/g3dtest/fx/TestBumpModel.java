package g3dtest.fx;

import com.g3d.app.SimpleApplication;
import com.g3d.light.DirectionalLight;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.Sphere;
import com.g3d.system.DisplaySettings;
import com.g3d.util.TangentBinormalGenerator;

public class TestBumpModel extends SimpleApplication {

    float angle;
    PointLight pl;
    Spatial lightMdl;

    public static void main(String[] args){
        TestBumpModel app = new TestBumpModel();
        app.setSettings(new DisplaySettings(DisplaySettings.Template.Default800x600));
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial signpost = manager.loadModel("signpost.obj");
        signpost.setMaterial(manager.loadMaterial("signpost.j3m"));
        rootNode.attachChild(signpost);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial(manager.loadMaterial("red_color.j3m"));
        rootNode.attachChild(lightMdl);

        // flourescent main light
        pl = new PointLight();
        pl.setColor(new ColorRGBA(0.88f, 0.92f, 0.95f, 1.0f));
        rootNode.addLight(pl);

        // sunset light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f,-0.7f,1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.44f, 0.30f, 0.20f, 1.0f));
        rootNode.addLight(dl);

        // skylight
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.6f,-1,-0.6f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.10f, 0.22f, 0.44f, 1.0f));
        rootNode.addLight(dl);

        // white ambient light
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, -0.5f,-0.1f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.50f, 0.40f, 0.50f, 1.0f));
        rootNode.addLight(dl);
    }

    @Override
    public void simpleUpdate(float tpf){
        angle += tpf * 0.25f;
        angle %= FastMath.TWO_PI;

        pl.setPosition(new Vector3f(FastMath.cos(angle) * 6f, 3f, FastMath.sin(angle) * 6f));
        lightMdl.setLocalTranslation(pl.getPosition());
    }

}
