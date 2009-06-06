package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.scene.Spatial;
import com.g3d.system.AppSettings;

/**
 * Tests OBJ format loading
 */
public class TestObjLoading extends SimpleApplication {

    public static void main(String[] args){
        TestObjLoading app = new TestObjLoading();
        app.setSettings(new AppSettings(AppSettings.Template.Default640x480));
        app.start();
    }

    public void simpleInitApp() {
        // create the geometry and attach it
        Spatial teapot = manager.loadModel("teapot.obj");

        // show normals as material
        Material mat = new Material(manager, "debug_normals.j3md");
        teapot.setMaterial(mat);
        
        rootNode.attachChild(teapot);
    }
}
