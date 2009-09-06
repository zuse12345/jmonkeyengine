package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.pack.J3PFileLocator;
import com.g3d.scene.Spatial;

public class TestSceneLoading extends SimpleApplication {

    public static void main(String[] args){
        TestSceneLoading app = new TestSceneLoading();
        app.start();
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(250);

        // create the geometry and attach it
        manager.registerLocator("town_lzma.j3p", J3PFileLocator.class, "scene", "meshxml",
                                                                  "material", "jpg");

        long time = System.nanoTime();
        Spatial cube = manager.loadModel("main.scene");
        long diff = System.nanoTime() - time;

        manager.clearCache();
        time = System.nanoTime();
        cube = manager.loadModel("main.scene");
        diff = System.nanoTime() - time;

        manager.clearCache();
        time = System.nanoTime();
        cube = manager.loadModel("main.scene");
        diff = System.nanoTime() - time;

        manager.clearCache();
        time = System.nanoTime();
        cube = manager.loadModel("main.scene");
        diff = System.nanoTime() - time;

        double billion = 1000000000.0;

        System.out.println("Time taken to load scene: "+(diff / billion)+" seconds");

        rootNode.attachChild(cube);
    }
}
