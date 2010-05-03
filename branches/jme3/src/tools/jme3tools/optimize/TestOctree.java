package jme3tools.optimize;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.MeshLoader;
import java.util.HashSet;
import java.util.Set;


public class TestOctree extends SimpleApplication {

    private Octree tree;
    private Set<Geometry> renderSet = new HashSet<Geometry>();

    public static void main(String[] args){
        TestOctree app = new TestOctree();
        app.start();
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(2000);
        this.cam.setFrustumFar(10000);
        MeshLoader.AUTO_INTERLEAVE = false;

        assetManager.registerLocator("quake3level.zip", "com.jme3.asset.plugins.JARLocator");
        Spatial scene = MeshLoader.loadModel(assetManager, "main.meshxml","Scene.material");
//        scene.setLocalScale(0.2f);
//        Spatial scene = manager.loadModel("models/teapot.obj");
//        Material mat = new Material(manager, "Common/MatDefs/Misc/ShowNormals.j3md");
//        scene.setMaterial(mat);

        // generate octree
        tree = new Octree(scene, 20000);
        tree.construct();
    }

    @Override
    public void simpleRender(RenderManager rm){
        renderSet.clear();
        tree.generateRenderSet(renderSet, cam);
        System.out.println("Geoms: "+renderSet.size());
        int tris = 0;

        for (Geometry geom : renderSet){
            tris += geom.getTriangleCount();
            viewPort.getQueue().addToQueue(geom, geom.getQueueBucket());
        }

        Matrix4f transform = new Matrix4f();
//        transform.setScale(0.2f, 0.2f, 0.2f);
        System.out.println("Tris: "+tris);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.White);
        tree.renderBounds(viewPort.getQueue(), transform, mat);

        rm.flushQueue(viewPort);
    }
}
