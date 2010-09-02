package jme3test.input;

import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;

/**
 * Tests OBJ format loading
 */
public class TestChaseCamera extends SimpleApplication {

    public static void main(String[] args){
        TestChaseCamera app = new TestChaseCamera();
        app.start();
    }

    public void simpleInitApp() {
        // Load a teapot model
        Geometry teaGeom = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.obj");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        teaGeom.setMaterial(mat);
        rootNode.attachChild(teaGeom);

        // Disable the flyby cam
        flyCam.setEnabled(false);

        // Enable a chase cam
        ChaseCamera chaseCam = new ChaseCamera(cam, teaGeom);
        chaseCam.setMaxDistance(3);
        chaseCam.setMinDistance(1);
        chaseCam.registerWithInput(inputManager);
    }
}
