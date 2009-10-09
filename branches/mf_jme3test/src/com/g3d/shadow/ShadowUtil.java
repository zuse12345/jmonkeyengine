/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.shadow;

import com.g3d.bounding.BoundingBox;
import com.g3d.bounding.BoundingSphere;
import com.g3d.bounding.BoundingVolume;
import com.g3d.math.FastMath;
import com.g3d.math.Matrix4f;
import com.g3d.math.Quaternion;
import com.g3d.math.Transform;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.queue.SpatialList;
import com.g3d.scene.Spatial;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Includes various useful shadow mapping functions.
 *
 * See: http://appsrv.cse.cuhk.edu.hk/~fzhang/pssm_vrcia/
 * for more info.
 */
public class ShadowUtil {

    /**
     * Updates the frustum splits stores in <code>splits</code> using PSSM.
     */
    public static void updateFrustumSplits(float[] splits, float near, float far, float lambda){
        for(int i = 0; i < splits.length; i++){
            float IDM = i / (float)splits.length;
            float log = near * FastMath.pow((far / near), IDM);
            float uniform = near + (far - near) * IDM;
            splits[i] = log * lambda + uniform * (1.0f - lambda);
        }

        // This is used to improve the correctness of the calculations. Our main near- and farplane
        // of the camera always stay the same, no matter what happens.
        splits[0] = near;
        splits[splits.length-1] = far;
    }

    /**
     * Updates the points array to contain the frustum corners of the given
     * camera. The nearOverride and farOverride variables can be used
     * to override the camera's near/far values with own values.
     *
     * TODO: Reduce creation of new vectors
     * 
     * @param viewCam
     * @param nearOverride
     * @param farOverride
     */
    public static void updateFrustumPoints(Camera viewCam,
                                     float nearOverride,
                                     float farOverride,
                                     float scale,
                                     Vector3f[] points){
        Vector3f pos = viewCam.getLocation();
        Vector3f dir = viewCam.getDirection();
        Vector3f up = viewCam.getUp();
        float near = nearOverride;
        float far = farOverride;
        float ftop = viewCam.getFrustumTop();
        float fright = viewCam.getFrustumRight();
        float ratio = fright / ftop;

        float near_height;
        float near_width;
        float far_height;
        float far_width;

        if (viewCam.isParallelProjection()){
            near_height = ftop;
            near_width = near_height * ratio;
            far_height = ftop;
            far_width = far_height * ratio;
        }else{
            near_height = ftop;
            near_width = near_height * ratio;
            far_height = (ftop / near) * far;
            far_width = far_height * ratio;
        }

        Vector3f right = dir.cross(up).normalizeLocal();

        Vector3f temp = new Vector3f();
        temp.set(dir).multLocal(far).addLocal(pos);
        Vector3f farCenter = temp.clone();
        temp.set(dir).multLocal(near).addLocal(pos);
        Vector3f nearCenter = temp.clone();

        Vector3f nearUp = temp.set(up).multLocal(near_height).clone();
        Vector3f farUp = temp.set(up).multLocal(far_height).clone();
        Vector3f nearRight = temp.set(right).multLocal(near_width).clone();
        Vector3f farRight = temp.set(right).multLocal(far_width).clone();

        points[0].set(nearCenter).subtractLocal(nearUp).subtractLocal(nearRight);
        points[1].set(nearCenter).addLocal(nearUp).subtractLocal(nearRight);
        points[2].set(nearCenter).addLocal(nearUp).addLocal(nearRight);
        points[3].set(nearCenter).subtractLocal(nearUp).addLocal(nearRight);

        points[4].set(farCenter).subtractLocal(farUp).subtractLocal(farRight);
        points[5].set(farCenter).addLocal(farUp).subtractLocal(farRight);
        points[6].set(farCenter).addLocal(farUp).addLocal(farRight);
        points[7].set(farCenter).subtractLocal(farUp).addLocal(farRight);

        if (scale != 1.0f){
            // find center of frustum
            Vector3f center = new Vector3f();
            for (Vector3f pt : points){
                center.addLocal(pt);
            }
            center.divideLocal(8f);

            Vector3f cDir = new Vector3f();
            for (Vector3f pt : points){
                cDir.set(pt).subtractLocal(center);
                cDir.multLocal(scale - 1.0f);
                pt.addLocal(cDir);
            }
        }
    }

    private static BoundingBox computeUnionBound(SpatialList list, Transform transform){
        BoundingBox bbox = new BoundingBox();
        for (int i = 0; i < list.size(); i++){
            BoundingVolume vol = list.get(i).getWorldBound();
            BoundingVolume newVol = vol.transform(transform);
            bbox.mergeLocal(newVol);
        }
        return bbox;
    }

    private static BoundingBox computeUnionBound(SpatialList list, Matrix4f mat){
        BoundingBox bbox = new BoundingBox();
        BoundingVolume store = null;
        for (int i = 0; i < list.size(); i++){
            BoundingVolume vol = list.get(i).getWorldBound();
            store = vol.transform(mat, store);
            bbox.mergeLocal(store);
        }
        return bbox;
    }

    private static BoundingBox computeBoundForPoints(Vector3f[] pts, Transform transform){
        Vector3f min = new Vector3f(Vector3f.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Vector3f.NEGATIVE_INFINITY);
        Vector3f temp = new Vector3f();
        for (int i = 0; i < pts.length; i++){
            transform.transformVector(pts[i], temp);
            min.minLocal(temp);
            max.maxLocal(temp);
        }
        Vector3f center = min.add(max).multLocal(0.5f);
        Vector3f extent = max.subtract(min).multLocal(0.5f);
        return new BoundingBox(center, extent.x, extent.y, extent.z);
    }

    private static BoundingBox computeBoundForPoints(Vector3f[] pts, Matrix4f mat){
        Vector3f min = new Vector3f(Vector3f.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Vector3f.NEGATIVE_INFINITY);
        Vector3f temp = new Vector3f();
        
        for (int i = 0; i < pts.length; i++){
            float w = mat.multProj(pts[i], temp);
            temp.x /= w;
            temp.y /= w;
            
            min.minLocal(temp);
            max.maxLocal(temp);
        }

        Vector3f center = min.add(max).multLocal(0.5f);
        Vector3f extent = max.subtract(min).multLocal(0.5f);
        return new BoundingBox(center, extent.x, extent.y, extent.z);
    }

    /**
     * Updates the shadow camera to properly contain the given
     * points (which contain the eye camera frustum corners) and the
     * shadow occluder objects.
     * 
     * @param occluders
     * @param lightCam
     * @param points
     */
    public static void updateShadowCamera(SpatialList occluders,
                                          SpatialList recievers,
                                          Camera shadowCam,
                                          Vector3f[] points){

        boolean ortho = shadowCam.isParallelProjection();

        shadowCam.setProjectionMatrix(null);

        if (ortho){
            shadowCam.setFrustum(-1, 1, -1, 1, 1, -1);
        }else{
            shadowCam.setFrustumPerspective(45, 1, 1, 150);
        }

        // create transform to rotate points to viewspace
        //Transform t = new Transform(shadowCam.getRotation());
        Matrix4f viewProjMatrix = shadowCam.getViewProjectionMatrix();

        BoundingBox casterBB   = computeUnionBound(occluders, viewProjMatrix);
        BoundingBox recieverBB = computeUnionBound(recievers, viewProjMatrix);
        BoundingBox splitBB    = computeBoundForPoints(points, viewProjMatrix);

        Vector3f casterMin = casterBB.getMin(null);
        Vector3f casterMax = casterBB.getMax(null);

        Vector3f recieverMin = recieverBB.getMin(null);
        Vector3f recieverMax = recieverBB.getMax(null);

        Vector3f splitMin = splitBB.getMin(null);
        Vector3f splitMax = splitBB.getMax(null);

//        splitMin.set(casterMin);
//        splitMax.set(casterMax);

        splitMin.x = FastMath.clamp(splitMin.x, -1, 1);
        splitMin.y = FastMath.clamp(splitMin.y, -1, 1);
        splitMax.x = FastMath.clamp(splitMax.x, -1, 1);
        splitMax.y = FastMath.clamp(splitMax.y, -1, 1);
        float far = splitMax.z + 1.0f + 1.5f;

        shadowCam.setFrustumPerspective(45, 1, 1, far);
        Matrix4f projMatrix = shadowCam.getProjectionMatrix();

        Vector3f cropMin = new Vector3f();
        Vector3f cropMax = new Vector3f();

        // IMPORTANT: Special handling for Z values
//        cropMin.x = max(max(casterMin.x, recieverMin.x), splitMin.x);
//        cropMax.x = min(min(casterMax.x, recieverMax.x), splitMax.x);
//
//        cropMin.y = max(max(casterMin.y, recieverMin.y), splitMin.y);
//        cropMax.y = min(min(casterMax.y, recieverMax.y), splitMax.y);
//
//        cropMin.z = min(casterMin.z, splitMin.z);
//        cropMax.z = min(recieverMax.z, splitMax.z);

        cropMin.set(splitMin);
        cropMax.set(splitMax);

//        cropMin.z = Math.min(cropMin.z, cropMax.z - cropMin.z - 1000);
//        cropMin.z = Math.max(10f, cropMin.z);

        // Create the crop matrix.
        float scaleX, scaleY, scaleZ;
        float offsetX, offsetY, offsetZ;

        scaleX = (2.0f) / (cropMax.x - cropMin.x);
        scaleY = (2.0f) / (cropMax.y - cropMin.y);
//        scaleZ = 2.0f / (cropMax.z - cropMin.z);
        offsetX = -0.5f * (cropMax.x + cropMin.x) * scaleX;
        offsetY = -0.5f * (cropMax.y + cropMin.y) * scaleY;
//        offsetZ = -0.5f * (cropMax.z + cropMin.z) * scaleZ;


//        offsetZ = -cropMin.z * scaleZ;

        Matrix4f cropMatrix = new Matrix4f(scaleX,  0f,      0f,      offsetX,
                                           0f,      scaleY,  0f,      offsetY,
                                           0f,      0f,      1f,      0f,
                                           0f,      0f,      0f,      1f);
//        cropMatrix.transposeLocal();
//        Matrix4f cropMatrix = new Matrix4f();
//        cropMatrix.setScale(new Vector3f(scaleX, scaleY, 1f));
//        cropMatrix.setTranslation(offsetX, offsetY, 0);

        Matrix4f result = new Matrix4f();
        result.set(cropMatrix);
        result.multLocal(projMatrix);
//        result.set(projMatrix);
//        result.multLocal(cropMatrix);
        shadowCam.setProjectionMatrix(result);

//        shadowCam.setFrustum(cropMin.z, cropMax.z, // near, far
//                             cropMin.x, cropMax.x, // left, right
//                             cropMax.y, cropMin.y); // top, bottom

        // compute size and center of final frustum
        //float sizeX   = (max.x - min.x) / 2f;
        //float sizeY   = (max.y - min.y) / 2f;
        //float offsetX = (max.x + min.x) / -2f;
        //float offsetY = (max.y + min.y) / -2f;

        // compute center for frustum
        //temp.set(offsetX, offsetY, 0);
        //invRot.mult(temp, temp);

        //shadowCam.setLocation(temp);
        //shadowCam.setFrustum(min.z, max.z, -sizeX, sizeX, sizeY, -sizeY);
    }
}
