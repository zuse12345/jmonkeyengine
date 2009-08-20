package com.g3d.collision.bih;

import com.g3d.bounding.BoundingBox;
import com.g3d.bounding.IntersectionRecord;
import com.g3d.math.Plane;
import com.g3d.math.Ray;
import com.g3d.math.Triangle;
import com.g3d.math.Vector3f;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Bounding Interval Hierarchy.
 * Based on:
 *
 * Instant Ray Tracing: The Bounding Interval Hierarchy
 * By Carsten Wächter and Alexander Keller
 */
public class BIHNode {

    Triangle[] triangles;
    BIHNode left;
    BIHNode right;
    float leftPlane;
    float rightPlane;
    int axis;

    private static final TriangleAxisComparator[] comparators = new TriangleAxisComparator[3];

    static {
        comparators[0] = new TriangleAxisComparator(0);
        comparators[1] = new TriangleAxisComparator(1);
        comparators[2] = new TriangleAxisComparator(2);
    }

    private static class TriangleAxisComparator implements Comparator<Triangle> {

        private final int axis;

        public TriangleAxisComparator(int axis){
            this.axis = axis;
        }

        @Override
        public int compare(Triangle o1, Triangle o2) {
            float v1, v2;
            Vector3f c1 = o1.getCenter();
            Vector3f c2 = o2.getCenter();
            switch (axis){
                case 0: v1 = c1.x; v2 = c2.x; break;
                case 1: v1 = c1.y; v2 = c2.y; break;
                case 2: v1 = c1.z; v2 = c2.z; break;
                default: assert false; return 0;
            }
            if (v1 > v2)
                return 1;
            else if (v1 < v2)
                return -1;
            else
                return 0;
        }
    }

    private static final float getExtreme(Triangle t, int axis, boolean left){
        float v1, v2, v3;
        switch (axis){
            case 0: v1 = t.get(0).x; v2 = t.get(1).x; v3 = t.get(2).x; break;
            case 1: v1 = t.get(0).y; v2 = t.get(1).y; v3 = t.get(2).y; break;
            case 2: v1 = t.get(0).z; v2 = t.get(1).z; v3 = t.get(2).z; break;
            default: assert false; return 0;
        }
        if (left){
            if (v1 < v2){
                if (v1 < v3)
                    return v1;
                else
                    return v3;
            }else{
                if (v2 < v3)
                    return v2;
                else
                    return v3;
            }
        }else{
            if (v1 > v2){
                if (v1 > v3)
                    return v1;
                else
                    return v3;
            }else{
                if (v2 > v3)
                    return v2;
                else
                    return v3;
            }
        }
    }

    public enum BIHResult {
        Left,
        Right,
        LeftThenRight,
        RightThenLeft,
        None
    }

    private class BIHStackData {
        BIHNode node;
        float min, max;
    }

//    public Vector3f intersect(Ray r, BoundingBox sceneBound){
//        Stack<BIHStackData> stack = new ArrayDeque();
//
//    }

    public BIHResult whichSide(Ray r){
        // test left plane
        float rPos;
        boolean neg;

        switch (axis){
            case 0:
                rPos = r.getOrigin().x;
                neg = r.getDirection().x < 0; break;
            case 1:
                rPos = r.getOrigin().y;
                neg = r.getDirection().y < 0; break;
            case 2:
                rPos = r.getOrigin().z;
                neg = r.getDirection().z < 0; break;
            default: 
                assert false;
                return null;
        }

        if (rPos < leftPlane){
            // origin is to the left
            if (neg)
                return BIHResult.Left; // ray on + side and not intersecting axis plane
            else
                return BIHResult.LeftThenRight; // ray intersecting left plane
        }else if (rightPlane < rPos){
            if (neg)
                return BIHResult.RightThenLeft; // ray intersecting right plane
            else
                return BIHResult.Right; // ray on - side and not intersecting axis plane
        }else{ // leftPlane < rPos < rightPlane
            // ray in between planes
            return null;
        }
    }

    public void subdivide(Triangle[] tris){
        // choose axis and plane split location
        BoundingBox bbox = new BoundingBox();
        bbox.computeFromTris(tris, 0, tris.length);

        // choose axis based on longest extent
        int axis;
        float middle;
        if (bbox.getXExtent() > bbox.getYExtent()){
            if (bbox.getXExtent() > bbox.getZExtent()){
                axis = 0;
                middle = bbox.getCenter().x;
            }else{
                axis = 2;
                middle = bbox.getCenter().z;
            }
        }else{
            if (bbox.getYExtent() > bbox.getZExtent()){
                axis = 1;
                middle = bbox.getCenter().y;
            }else{
                axis = 2;
                middle = bbox.getCenter().z;
            }
        }

        // sort triangles from left to right along axis
        TriangleAxisComparator comparator = comparators[axis];
        Arrays.sort(tris, comparator);
        int splitIndex = -1;

        loop: for (int i = 0; i < tris.length; i++){
            Triangle tri = tris[i];
            float v;
            switch (axis){
                case 0: v = tri.getCenter().x; break;
                case 1: v = tri.getCenter().y; break;
                case 2: v = tri.getCenter().z; break;
                default: assert false; return;
            }
            if (v > middle){
                if (i == 0){
                    // no left plane
                    splitIndex = -2;
                }else{
                    splitIndex = i;
                }
            }
        }
        
        if (splitIndex == -1){
            // all triangles are to the left
            // get the rightmost extreme of the last triangle
            leftPlane = getExtreme(tris[tris.length-1], axis, false);
            rightPlane = Float.NaN; // right plane doesn't exist

        }else if (splitIndex == -2){
            // all triangles are to the right
            // get the leftmost extreme of the first triangle
            leftPlane = Float.NaN; // left plane doesn't exist
            rightPlane = getExtreme(tris[0], axis, true);
        }


    }

}
