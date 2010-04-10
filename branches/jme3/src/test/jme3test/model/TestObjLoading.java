package jme3test.model;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;

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
