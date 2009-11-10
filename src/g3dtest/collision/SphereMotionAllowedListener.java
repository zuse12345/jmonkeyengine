package g3dtest.collision;

import com.g3d.collision.CollisionResult;
import com.g3d.collision.CollisionResults;
import com.g3d.collision.MotionAllowedListener;
import com.g3d.collision.SweepSphere;
import com.g3d.math.Plane;
import com.g3d.math.Vector3f;
import com.g3d.scene.Spatial;

public class SphereMotionAllowedListener implements MotionAllowedListener {

    private SweepSphere ss = new SweepSphere();
    private CollisionResults results = new CollisionResults();
    private Spatial scene;
    private Vector3f dimension = new Vector3f();

    private Vector3f newPos = new Vector3f();
    private Vector3f newVel = new Vector3f();

    final float unitsPerMeter = 10.0f;
    final float unitScale = unitsPerMeter / 100.0f;
    final float veryCloseDist = 0.005f * unitScale;

    private int depth = 0;

    public SphereMotionAllowedListener(Spatial scene, Vector3f dimension){
        if (scene == null || dimension == null)
            throw new NullPointerException();

        this.scene = scene;
        this.dimension.set(dimension);
    }

    private void collideWithWorld(){
        if (depth > 5)
            return;

        ss.setCenter(newPos);
        ss.setVelocity(newVel);
        ss.setDimension(1.25f);

        results.clear();
        scene.collideWith(ss, results);

        if (results.size() == 0){
            newPos.addLocal(newVel);
            return;
        }

        // *** collision occured ***
        Vector3f destination = newPos.add(newVel);
        CollisionResult closest = results.getClosestCollision();
        Vector3f contactPoint = closest.getContactPoint().clone();
        float dist = closest.getDistance();

        if (dist >= veryCloseDist){
            // P += ||V|| * dist
            Vector3f tmp = new Vector3f(newVel);
            tmp.normalizeLocal().multLocal(dist - veryCloseDist);
            newPos.addLocal(tmp);

            tmp.normalizeLocal();
            tmp.multLocal(veryCloseDist);
            contactPoint.subtractLocal(tmp);
        }


        
//        Vector3f normal = newPos.subtract(contactPoint).normalizeLocal();
        Vector3f normal = closest.getContactNormal();

        Plane p = new Plane();
        p.setOriginNormal(contactPoint, normal);
        normal.multLocal(normal.dot(destination) -  veryCloseDist);
//        normal.multLocal(p.pseudoDistance(destination));
        Vector3f newDest = destination.subtract(normal);
        newVel.set(newDest).subtractLocal(contactPoint);
        
        // recurse:
        if (newVel.length() < veryCloseDist){
            return;
        }

        depth = depth + 1;
        collideWithWorld();
    }

    public void checkMotionAllowed(Vector3f position, Vector3f velocity) {
        depth = 0;
        newPos.set(position);
        newVel.set(velocity);
        collideWithWorld();
        position.set(newPos);
    }


}
