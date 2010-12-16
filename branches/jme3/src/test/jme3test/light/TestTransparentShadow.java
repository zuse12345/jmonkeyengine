package jme3test.light;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.GL11;

public class TestTransparentShadow extends SimpleApplication {

    public static void main(String[] args){
        TestTransparentShadow app = new TestTransparentShadow();
        app.start();
    }

    public void simpleInitApp() {

        GL11.glEnable(ARBMultisample.GL_SAMPLE_ALPHA_TO_COVERAGE_ARB);

        // create the geometry and attach it
        Spatial teaGeom = assetManager.loadModel("Models/Tree/Tree2.mesh.xml");

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, 1, 1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.198f, 0.136f, 0.618f, 1f).mult(2.1f));
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.965f, 0.949f, 0.772f, 1f).mult(2.1f));
        rootNode.addLight(dl);
        // show normals as material
//        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
//        teaGeom.setMaterial(mat);

        rootNode.attachChild(teaGeom);
    }
}
