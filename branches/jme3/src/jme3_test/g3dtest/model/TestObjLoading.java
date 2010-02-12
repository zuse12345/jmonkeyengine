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
        Mesh teaMesh = teaGeom.getMesh();
        teaMesh.updateBound();
        FloatToFixed.convertToFixed(teaGeom);
//        ModelConverter.generateStrips(teaMesh, true, false, 24, 0);
        ModelConverter.compressIndexBuffer(teaMesh);
        teaMesh.setInterleaved();
//        teaMesh.setStatic();
//        teaMesh.setStreamed();

        Transform originalTransform = teaGeom.getTransform();

        // show normals as material
        Material mat = new Material(manager, "debug_normals.j3md");

        for (int y = -10; y < 10; y++){
            for (int x = -10; x < 10; x++){
                Geometry teaClone = new Geometry("teapot", teaMesh);
                teaClone.setMaterial(mat);
                teaClone.setLocalTranslation(x * .5f, 0, y * .5f);
                teaClone.setLocalScale(.5f);
                Transform cloneTransform = teaClone.getTransform();

                Transform newTrans = originalTransform.clone();
                newTrans.combineWithParent(cloneTransform);

                teaClone.setTransform(newTrans);

                rootNode.attachChild(teaClone);
            }
        }

        cam.setLocation(new Vector3f(8.378951f, 5.4324f, 8.795956f));
        cam.setRotation(new Quaternion(-0.083419204f, 0.90370524f, -0.20599906f, -0.36595422f));
    }
}
