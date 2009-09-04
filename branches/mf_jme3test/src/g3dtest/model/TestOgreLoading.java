package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.light.PointLight;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;

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
        Spatial cube = manager.loadModel("Cube.meshxml");
        rootNode.attachChild(cube);
    }
}
