package g3dtest.collision;

import com.g3d.app.SimpleApplication;
import com.g3d.collision.CollisionResult;
import com.g3d.collision.CollisionResults;
import com.g3d.collision.MotionAllowedListener;
import com.g3d.collision.SweepSphere;
import com.g3d.math.FastMath;
import com.g3d.math.Plane;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Sphere;
import com.g3d.system.AppSettings;

/**
 * Test simple collision with a plane.
 */
public class TestSimpleCollision extends SimpleApplication {

    public static void main(String[] args){
        TestSimpleCollision app = new TestSimpleCollision();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere q = new Sphere(32, 32, 2);
        
//        Quad q = new Quad(5, 5);
        Geometry g = new Geometry("Quad Geom", q);
//        g.rotate(FastMath.HALF_PI, 0, FastMath.PI);
        g.setMaterial(manager.loadMaterial("jme_logo.j3m"));
        rootNode.attachChild(g);

        flyCam.setMotionAllowedListener(new ColListener());
    }

    private class ColListener implements MotionAllowedListener {

        private SweepSphere ss = new SweepSphere();
        private CollisionResults results = new CollisionResults();

        final float unitsPerMeter = 1.0f;
        final float unitScale = unitsPerMeter / 100.0f;
        final float veryCloseDist = 0.005f * unitScale;

        private int depth = 0;

        private Vector3f collideWithWorld(Vector3f pos, Vector3f vel){
            if (depth > 5)
                return pos.clone();

            ss.setCenter(pos);
            ss.setVelocity(vel);
            ss.setDimension(1.25f);

            results.clear();
            rootNode.collideWith(ss, results);

            if (results.size() == 0)
                return pos.add(vel);

            // *** collision occured ***
            Vector3f newPos = pos.clone();

            CollisionResult closest = results.getClosestCollision();
            Vector3f contactPoint = closest.getContactPoint().clone();
            float dist = closest.getDistance();

            if (dist >= veryCloseDist){
                Vector3f newVel = vel.clone();
                newVel.normalizeLocal().multLocal(dist - veryCloseDist);
                newPos = pos.add(newVel);

                newVel.normalizeLocal();
                newVel.multLocal(veryCloseDist);
                contactPoint.subtractLocal(newVel);
            }

            Plane p = new Plane();
            p.setOriginNormal(contactPoint, closest.getContactNormal());

            Vector3f dest = pos.add(vel);
            Vector3f antiNorm = new Vector3f(p.getNormal()).multLocal(p.pseudoDistance(dest));
            dest.subtractLocal(antiNorm);

            Vector3f newVel = dest.subtract(contactPoint);
            newPos.set(pos);

//            Vector3f tmp = new Vector3f(closest.getContactNormal());
//            tmp.multLocal(closest.getContactNormal().dot(vel));
//            newVel.set(vel).subtractLocal(tmp);
            
            // recurse:
            if (newVel.length() < veryCloseDist){
                return newPos;
            }

            depth = depth + 1;
            return collideWithWorld(newPos, newVel);
        }

        public void checkMotionAllowed(Vector3f position, Vector3f velocity) {
            depth = 0;
            Vector3f pos = collideWithWorld(position, velocity);
            position.set(pos);
        }
    }

}