
package g3dtest.light;

import com.g3d.app.SimpleApplication;
import com.g3d.light.DirectionalLight;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Spatial;
import com.g3d.scene.shape.Quad;
import com.g3d.scene.shape.Sphere;
import com.g3d.util.TangentBinormalGenerator;

public class TestTangentGen extends SimpleApplication {

    float angle;
    PointLight pl;
    Geometry lightMdl;

    public static void main(String[] args){
        TestTangentGen app = new TestTangentGen();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere sphereMesh = new Sphere(32, 32, 1);
        sphereMesh.setTextureMode(Sphere.TextureMode.Projected);
        sphereMesh.updateGeometry(32, 32, 1, false);
        addMesh("Sphere", sphereMesh, new Vector3f(-1, 0, 0));

        Quad quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1);
        addMesh("Quad", quadMesh, new Vector3f(1, 0, 0));
        
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1,-1,-1).normalizeLocal());
        dl.setColor(ColorRGBA.White);
        rootNode.addLight(dl);
    }

    private void addMesh(String name, Mesh mesh, Vector3f translation) {
        TangentBinormalGenerator.generate(mesh);

        Geometry testGeom = new Geometry(name, mesh);
        Material mat = manager.loadMaterial("tangentBinormal.j3m");
        testGeom.setMaterial(mat);
        testGeom.getLocalTranslation().set(translation);
        rootNode.attachChild(testGeom);

        Geometry debug = new Geometry(
                "Debug " + name,
                TangentBinormalGenerator.genTbnLines(mesh, 0.08f)
        );
        Material debugMat = manager.loadMaterial("vertex_color.j3m");
        debug.setMaterial(debugMat);
        debug.setCullHint(Spatial.CullHint.Never);
        debug.getLocalTranslation().set(translation);
        rootNode.attachChild(debug);
    }

    @Override
    public void simpleUpdate(float tpf){
    }

}
