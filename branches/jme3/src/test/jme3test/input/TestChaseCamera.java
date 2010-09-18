package jme3test.input;

import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

/**
 * Tests OBJ format loading
 */
public class TestChaseCamera extends SimpleApplication implements AnalogListener {

    private Geometry teaGeom;
    private ChaseCamera chaseCam;
    public static void main(String[] args){
        TestChaseCamera app = new TestChaseCamera();
        app.start();
    }

    public void simpleInitApp() {
        // Load a teapot model
        teaGeom = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.obj");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        teaGeom.setMaterial(mat);
        rootNode.attachChild(teaGeom);
        mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        Geometry ground=new Geometry("ground", new Quad(50, 50));
        ground.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        ground.setLocalTranslation(-25, -1, 25);
        ground.setMaterial(mat);
        rootNode.attachChild(ground);
        // Disable the flyby cam
        flyCam.setEnabled(false);

        // Enable a chase cam
        chaseCam = new ChaseCamera(cam, teaGeom,inputManager);
        regsiterInput();
    }

    public void regsiterInput(){
        inputManager.addMapping("moveForward",new KeyTrigger(keyInput.KEY_UP));
        inputManager.addMapping("moveBackward",new KeyTrigger(keyInput.KEY_DOWN));
        inputManager.addMapping("moveRight",new KeyTrigger(keyInput.KEY_RIGHT));
        inputManager.addMapping("moveLeft",new KeyTrigger(keyInput.KEY_LEFT));
        inputManager.addListener(this, "moveForward","moveBackward","moveRight","moveLeft");
    }

    public void onAnalog(String name, float value, float tpf) {
        if(name.equals("moveForward")){
            teaGeom.move(0, 0, -5*tpf);
        }
        if(name.equals("moveBackward")){
            teaGeom.move(0, 0, 5*tpf);
        }
        if(name.equals("moveRight")){
            teaGeom.move(5*tpf, 0, 0);
        }
        if(name.equals("moveLeft")){
            teaGeom.move(-5*tpf, 0, 0);
        }

    }

    


}
