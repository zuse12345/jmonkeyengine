package g3dtools.optimize;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.pack.J3PFileLocator;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Matrix4f;
import com.g3d.renderer.RenderManager;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.plugins.ogre.MeshLoader;
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

        manager.registerLocator("Q3.j3p", "com.g3d.asset.pack.J3PFileLocator", "tga", "meshxml", "material");
        Spatial scene = MeshLoader.loadModel(manager, "main.meshxml","Scene.material");
//        scene.setLocalScale(0.2f);
//        Spatial scene = manager.loadModel("teapot.obj");
//        Material mat = new Material(manager, "debug_normals.j3md");
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
        Material mat = new Material(manager, "wire_color.j3md");
        mat.setColor("m_Color", ColorRGBA.White);
        tree.renderBounds(viewPort.getQueue(), transform, mat);

        rm.flushQueue(viewPort);
    }
}
