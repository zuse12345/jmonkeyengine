package g3dtest.collision;

import com.g3d.collision.CollisionResult;
import com.g3d.collision.CollisionResults;
import com.g3d.collision.MotionAllowedListener;
import com.g3d.collision.SweepSphere;
import com.g3d.math.FastMath;
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

    final float unitsPerMeter = 100.0f;
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

        if (newVel.length() < FastMath.FLT_EPSILON)
            return;

        Vector3f destination = newPos.add(newVel);

        ss.setCenter(newPos);
        ss.setVelocity(newVel);
        ss.setDimension(dimension);

        results.clear();
        scene.collideWith(ss, results);

        if (results.size() == 0){
            newPos.addLocal(newVel);
            return;
        }

        for (int i = 0; i < 1; i++){
            CollisionResult collision = results.getCollision(i);
            // *** collision occured ***
            Vector3f destination = newPos.add(newVel);
            Vector3f contactPoint = collision.getContactPoint().clone();
            float dist = collision.getDistance();

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
            Vector3f normal = collision.getContactNormal();

            Plane p = new Plane();
            p.setOriginNormal(contactPoint, normal);

            Vector3f destinationOnPlane = p.getClosestPoint(destination);
            newVel.set(destinationOnPlane).subtractLocal(contactPoint);
    //        normal.multLocal(normal.dot(destination) -  veryCloseDist);
    ////        normal.multLocal(p.pseudoDistance(destination));
    //        Vector3f newDest = destination.add(normal);
    //        newVel.set(newDest).subtractLocal(contactPoint);

            // recurse:
            if (newVel.length() < veryCloseDist){
                return;
            }
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
