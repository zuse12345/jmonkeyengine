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
import com.g3d.util.TangentBinormalGenerator;

public class TestNormalMapping extends SimpleApplication {

    float angle;
    PointLight pl;
    Spatial lightMdl;

    public static void main(String[] args){
        TestNormalMapping app = new TestNormalMapping();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere sphMesh = new Sphere(32, 32, 1);
        sphMesh.setTextureMode(Sphere.TextureMode.Projected);
        sphMesh.updateGeometry(32, 32, 1, false);
        TangentBinormalGenerator.generate(sphMesh);
        Geometry sphere = new Geometry("Rock Ball", sphMesh);
        Material mat = manager.loadMaterial("pond_rock.j3m");
        sphere.setMaterial(mat);
        rootNode.attachChild(sphere);

//        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
//        lightMdl.setMaterial(manager.loadMaterial("red_color.j3m"));
//        rootNode.attachChild(lightMdl);

        pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        pl.setPosition(new Vector3f(0f, 0f, -4f));
        rootNode.addLight(pl);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1,-1,1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.22f, 0.15f, 0.1f, 1.0f));
        rootNode.addLight(dl);
    }

//    @Override
//    public void simpleUpdate(float tpf){
//        angle += tpf * 0.25f;
//        angle %= FastMath.TWO_PI;
//
//        pl.setPosition(new Vector3f(FastMath.cos(angle) * 4f, 0.5f, FastMath.sin(angle) * 4f));
//        lightMdl.setLocalTranslation(pl.getPosition());
//    }

}
