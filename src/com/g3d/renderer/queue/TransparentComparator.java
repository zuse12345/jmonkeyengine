package com.g3d.renderer.queue;

import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Spatial;
import com.g3d.util.TempVars;
import java.util.Comparator;

public class TransparentComparator implements Comparator<Spatial> {

    private Renderer renderer;

    public TransparentComparator(Renderer renderer){
        this.renderer = renderer;
    }

    /**
     * Calculates the distance from a spatial to the camera. Distance is a
     * squared distance.
     *
     * @param spat
     *            Spatial to distancize.
     * @return Distance from Spatial to camera.
     */
    private float distanceToCam(Camera cam, Spatial spat) {
        if (spat.queueDistance != Float.NEGATIVE_INFINITY)
                return spat.queueDistance;

        spat.queueDistance = 0;

        Vector3f camPosition = cam.getLocation();
        Vector3f spatPosition = null;
        Vector3f viewVector = cam.getDirection();
        Vector3f tempVector = TempVars.get().vect1;

        if (Vector3f.isValidVector(cam.getLocation())) {
            if (spat.getWorldBound() != null && Vector3f.isValidVector(spat.getWorldBound().getCenter()))
                spatPosition = spat.getWorldBound().getCenter();
            else if (spat instanceof Spatial && Vector3f.isValidVector(((Spatial)spat).getWorldTranslation()))
                spatPosition = ((Spatial) spat).getWorldTranslation();
        }

        if (spatPosition != null) {
            spatPosition.subtract(camPosition, tempVector);

            float retval = Math.abs(tempVector.dot(viewVector)
                    / viewVector.dot(viewVector));
            tempVector = viewVector.mult(retval, tempVector);

            spat.queueDistance = tempVector.length();
        }

        return spat.queueDistance;
    }

        public int compare(Spatial o1, Spatial o2) {
            Camera cam = renderer.getCamera();
            float d1 = distanceToCam(cam, o1);
            float d2 = distanceToCam(cam, o2);
            if (d1 == d2)
                return 0;
            else if (d1 < d2)
                return 1;
            else
                return -1;
        }
}
