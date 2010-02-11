package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.light.PointLight;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;
import com.g3d.scene.plugins.ogre.OgreMaterialList;
import com.g3d.scene.plugins.ogre.OgreMeshKey;

public class TestOgreLoading extends SimpleApplication {

    public static void main(String[] args){
        TestOgreLoading app = new TestOgreLoading();
        app.start();
    }

    public void simpleInitApp() {
        PointLight pl = new PointLight();
        pl.setPosition(new Vector3f(10, 10, -10));
        rootNode.addLight(pl);

        // create the geometry and attach it
        OgreMaterialList matList = (OgreMaterialList) manager.loadContent("Scene.material");
        OgreMeshKey key = new OgreMeshKey("Cube.meshxml", matList);
        Spatial cube = (Spatial) manager.loadContent(key);
        rootNode.attachChild(cube);
    }
}
