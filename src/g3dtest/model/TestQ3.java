package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.pack.J3PFileLocator;
import com.g3d.collision.CollisionResult;
import com.g3d.collision.CollisionResults;
import com.g3d.collision.MotionAllowedListener;
import com.g3d.collision.SweepSphere;
import com.g3d.material.Material;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.plugins.ogre.MeshLoader;
import com.g3d.scene.shape.Sphere;
import com.g3d.texture.Texture;

public class TestQ3 extends SimpleApplication {

    private Sphere sphereMesh = new Sphere(32, 32, 10, false, true);
    private Geometry sphere = new Geometry("Sky", sphereMesh);
    private Spatial gameLevel;

    public static void main(String[] args){
        TestQ3 app = new TestQ3();
        app.start();
    }

    public void simpleInitApp() {
        MeshLoader.AUTO_INTERLEAVE = false;
        this.flyCam.setMoveSpeed(500);
        this.cam.setFrustumFar(5000);

        // load sky
        sphere.updateModelBound();
        sphere.setQueueBucket(Bucket.Sky);
        Material sky = new Material(manager, "sky.j3md");
        Texture tex = manager.loadTexture("sky3.dds", false, true, true, 0);
        sky.setTexture("m_Texture", tex);
        sphere.setMaterial(sky);
        rootNode.attachChild(sphere);

        // create the geometry and attach it
        manager.registerLocator("Q3.j3p", J3PFileLocator.class, "tga", "meshxml", "material");

        // create the geometry and attach it
        gameLevel = manager.loadOgreModel("main.meshxml","Scene.material");
        gameLevel.updateGeometricState();
//        gameLevel.setLocalScale(0.2f);
        rootNode.attachChild(gameLevel);

        flyCam.setMotionAllowedListener(new SSListener());
    }

    private class SSListener implements MotionAllowedListener {

        private SweepSphere ss = new SweepSphere();
        private CollisionResults results = new CollisionResults();

        public void checkMotionAllowed(Vector3f position, Vector3f velocity) {
            ss.setDimension(10);
            ss.setVelocity(velocity);
            ss.setCenter(position);

            results.clear();
            gameLevel.collideWith(ss, results);

            if (results.size() > 0){
                CollisionResult res = results.getClosestCollision();
                float dist = res.getDistance();
                if (dist > FastMath.ZERO_TOLERANCE)
                    dist -= FastMath.ZERO_TOLERANCE;
                
                Vector3f tempVel = velocity.normalize().multLocal(dist);
                position.addLocal(tempVel);
            }else{
                position.addLocal(velocity);
            }
        }

    }

    @Override
    public void simpleUpdate(float tpf){
        sphere.setLocalTranslation(cam.getLocation());
    }
}
