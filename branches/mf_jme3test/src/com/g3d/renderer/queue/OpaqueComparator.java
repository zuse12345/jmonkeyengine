package com.g3d.renderer.queue;

import com.g3d.material.Material;
import com.g3d.material.Technique;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Spatial;
import com.g3d.util.TempVars;
import java.util.Comparator;

public class OpaqueComparator implements Comparator<Spatial> {

    private Renderer renderer;

    public OpaqueComparator(Renderer renderer){
        this.renderer = renderer;
    }

    public float distanceToCam(Spatial spat, Vector3f tempVec){
        if (spat == null)
            return Float.NEGATIVE_INFINITY;

        if (spat.queueDistance != Float.NEGATIVE_INFINITY)
                return spat.queueDistance;

        Camera cam = renderer.getCamera();

        Vector3f camPosition = cam.getLocation();
        Vector3f viewVector = cam.getDirection();
        Vector3f spatPosition = null;

        if (spat.getWorldBound() != null){
            spatPosition = spat.getWorldBound().getCenter();
        }else{
            spatPosition = spat.getWorldTranslation();
        }

        spatPosition.subtract(camPosition, tempVec);
        spat.queueDistance = tempVec.dot(tempVec);

        float retval = Math.abs(tempVec.dot(viewVector)
                / viewVector.dot(viewVector));
        tempVec = viewVector.mult(retval, tempVec);

        spat.queueDistance = tempVec.length();

        return spat.queueDistance;
    }

    public int compare(Spatial o1, Spatial o2) {
        if (o1 == null || o2 == null)
            return -1;

//        assert TempVars.get().lock();
//        Vector3f tempVec = TempVars.get().vect1;
//        float d1 = distanceToCam(o1, tempVec);
//        float d2 = distanceToCam(o2, tempVec);
//        assert TempVars.get().unlock();

        Material m1 = o1.getMaterial();
        Material m2 = o2.getMaterial();
        Technique t1 = m1.getActiveTechnique();
        Technique t2 = m2.getActiveTechnique();
        int sid1 = -1, sid2 = -1;
        if (t1 != null){
            if (t1.getShader() != null){
                sid1 = t1.getShader().getId();
            }
        }
        if (t2 != null){
            if (t2.getShader() != null){
                sid2 = t2.getShader().getId();
            }
        }
        sid1++; sid2++;
//        sid1 *= 1000;
//        sid2 *= 1000;
//        float near = renderer.getCamera().getFrustumNear();
//        float far  = renderer.getCamera().getFrustumFar();
//        d1 = (d1 - near) / far;
//        d2 = (d2 - near) / far;
//        sid1 += d1 * 1000;
//        sid2 += d2 * 1000;

        if (sid1 == sid2)
            return 0;
        else if (sid1 < sid2)
            return 1;
        else
            return -1;
    }
}
