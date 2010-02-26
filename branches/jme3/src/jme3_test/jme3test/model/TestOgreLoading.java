package jme3test.model;

import com.jme3.app.SimpleApplication;
import com.jme3.light.PointLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMaterialList;
import com.jme3.scene.plugins.ogre.OgreMeshKey;

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
