package jme3test.model;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMeshKey;

public class TestLevelOfDetail extends SimpleApplication {

    private Spatial signpost;

    public static void main(String[] args){
        TestLevelOfDetail app = new TestLevelOfDetail();
        app.start();
    }

    public void simpleUpdate(float tpf){
        BoundingVolume v = signpost.getWorldBound();
        if (v == null)
            return;
        
        float d = v.distanceToEdge(cam.getLocation());
        signpost.setLodLevel( (int) FastMath.floor(d / 5f) );
    }

    public void simpleInitApp() {
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(-1, -1, -1).normalize());
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(0, -.5f, .5f).normalize());
        rootNode.addLight(dl);

        OgreMeshKey key = new OgreMeshKey("signpost2.meshxml", null);
        signpost = (Spatial) manager.loadContent(key);
        signpost.setMaterial(manager.loadMaterial("signpost.j3m"));
        rootNode.attachChild(signpost);
        
        cam.setLocation(new Vector3f(5.5618367f, 3.82252f, 4.8586326f));
        cam.setRotation(new Quaternion(-0.060311187f, 0.89377415f, -0.12652166f, -0.42605457f));
    }
}
