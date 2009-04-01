package com.g3d.test;

import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.SceneManager;
import com.g3d.scene.Spatial;
import com.g3d.app.Application;
import com.g3d.app.SimpleApplication;
import com.g3d.scene.material.NormalMaterial;
import com.g3d.util.ObjLoader;

/**
 * Tests OBJ format loading
 */
public class TestObjLoading extends SimpleApplication {

    public static void main(String[] args){
        TestObjLoading app = new TestObjLoading();
        app.start();
    }

    public void simpleInitApp() {
        // create obj loader
        ObjLoader objLoader = new ObjLoader();

        // read model
        objLoader.read(TestObjLoading.class.getResource("/com/g3d/test/data/teapot2.obj"));

        // create the geometry and attach it
        Geometry teapot = new Geometry("teapot model", objLoader.getMesh());

        // show normals as material
        teapot.setMaterial(new NormalMaterial());
        rootNode.attachChild(teapot);
    }
}
