package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.scene.Spatial;

public class TestOgreLoading extends SimpleApplication {

    public static void main(String[] args){
        TestOgreLoading app = new TestOgreLoading();
        app.start();
    }

    public void simpleInitApp() {
        // create the geometry and attach it
        Spatial teapot = manager.loadModel("Cube.mesh.xml");

//        // show normals as material
//        Material mat = new Material(manager, "debug_normals.j3md");
//        teapot.setMaterial(mat);

//        rootNode.attachChild(teapot);
    }
}
