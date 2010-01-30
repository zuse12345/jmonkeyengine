package g3dtest.export;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.pack.J3PFileLocator;
import com.g3d.scene.Spatial;
import com.g3d.scene.plugins.ogre.MeshLoader;

public class TestSceneExport extends SimpleApplication {

    private Spatial gameLevel;

    public static void main(String[] args){
        TestSceneExport app = new TestSceneExport();
        app.start();
    }

    public void simpleInitApp() {
        MeshLoader.AUTO_INTERLEAVE = false;
//        this.flyCam.setMoveSpeed(500);

        this.cam.setFrustumFar(2000);

        // create the geometry and attach it
        manager.registerLocator("data.j3p", J3PFileLocator.class, "dds", "meshxml", "material");

        // create the geometry and attach it
        gameLevel = manager.loadOgreModel("main.meshxml","Scene.material");
        gameLevel.updateGeometricState();
//        gameLevel.setLocalScale(0.2f);
        rootNode.attachChild(gameLevel);
        rootNode.updateGeometricState();

    }

}
