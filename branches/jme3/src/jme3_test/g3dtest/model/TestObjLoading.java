package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Quaternion;
import com.g3d.math.Transform;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import g3dtools.converters.model.FloatToFixed;
import g3dtools.converters.model.ModelConverter;

/**
 * Tests OBJ format loading
 */
public class TestObjLoading extends SimpleApplication {

    public static void main(String[] args){
        TestObjLoading app = new TestObjLoading();
        app.start();
    }

    public void simpleInitApp() {
        // create the geometry and attach it
        Geometry teaGeom = (Geometry) manager.loadModel("teapot.obj");
        
        // show normals as material
        Material mat = new Material(manager, "debug_normals.j3md");
        teaGeom.setMaterial(mat);

        rootNode.attachChild(teaGeom);
    }
}
