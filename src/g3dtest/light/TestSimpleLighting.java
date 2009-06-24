package g3dtest.light;

import com.g3d.app.SimpleApplication;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.shape.Sphere;
import com.g3d.util.TangentBinormalGenerator;

public class TestSimpleLighting extends SimpleApplication {

    float angle;
    PointLight pl;
    Geometry lightMdl;

    public static void main(String[] args){
        TestSimpleLighting app = new TestSimpleLighting();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Spatial teapot = manager.loadModel("teapot.obj");
        if (teapot instanceof Geometry){
            Geometry g = (Geometry) teapot;
            TangentBinormalGenerator.generate(g.getMesh());
        }else{
            throw new RuntimeException();
        }
        teapot.setLocalScale(2f);
        Material mat = new Material(manager, "phong_lighting.j3md");
        mat.setFloat("m_Shininess", 32f);
        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial(manager.loadMaterial("red_color.j3m"));
        lightMdl.getMesh().setStatic();
        rootNode.attachChild(lightMdl);

        pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        //pl.setRadius(3f);
        rootNode.addLight(pl);
    }

    @Override
    public void simpleUpdate(float tpf){
        angle += tpf;
        angle %= FastMath.TWO_PI;
        
        pl.setPosition(new Vector3f(FastMath.cos(angle) * 2f, 0.5f, FastMath.sin(angle) * 2f));
        lightMdl.setLocalTranslation(pl.getPosition());
    }

}
