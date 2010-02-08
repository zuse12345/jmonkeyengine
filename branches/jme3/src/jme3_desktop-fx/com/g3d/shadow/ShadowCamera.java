package com.g3d.shadow;

import com.g3d.light.DirectionalLight;
import com.g3d.light.Light;
import com.g3d.light.PointLight;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;

public class ShadowCamera {

    private Vector3f[] points = new Vector3f[8];
    private Light target;

    public ShadowCamera(Light target){
        this.target = target;
        for (int i = 0; i < points.length; i++){
            points[i] = new Vector3f();
        }
    }

    /**
     * Updates the camera view direction and position based on the light
     */
    private void updateLightCamera(Camera lightCam){
        if (target.getType() == Light.Type.Directional){
            DirectionalLight dl = (DirectionalLight) target;
            lightCam.setParallelProjection(true);
            lightCam.setLocation(Vector3f.ZERO);
            lightCam.setDirection(dl.getDirection());
            lightCam.setFrustum(-1, 1, -1, 1, 1, -1);
        }else{
            PointLight pl = (PointLight) target;
            lightCam.setParallelProjection(false);
            lightCam.setLocation(pl.getPosition());
            // direction will have to be calculated automatically
            lightCam.setFrustumPerspective(45, 1, 1, 300);
        }
    }

}
