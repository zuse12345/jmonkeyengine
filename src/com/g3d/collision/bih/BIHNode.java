package com.g3d.collision.bih;

import com.g3d.bounding.BoundingBox;
import com.g3d.collision.TrianglePickResults;
import com.g3d.math.Ray;
import com.g3d.math.Vector3f;
import com.g3d.util.TempVars;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.util.Stack;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * Bounding Interval Hierarchy.
 * Based on:
 *
 * Instant Ray Tracing: The Bounding Interval Hierarchy
 * By Carsten Wächter and Alexander Keller
 */
public class BIHNode {

    BIHTriangle[] triangles;
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

    public BIHNode(BIHTriangle[] triangles, int axis){
        this.triangles = triangles;
        this.axis = axis;
    }

    private static class TriangleAxisComparator implements Comparator<BIHTriangle> {

        private final int axis;

        public TriangleAxisComparator(int axis){
            this.axis = axis;
        }

        public int compare(BIHTriangle o1, BIHTriangle o2) {
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

//    private static final float getArea(Triangle t){
//        Vector3f tmpA = TempVars.get().vect1;
//        Vector3f tmpB = TempVars.get().vect2;
//
//        tmpA.set(t.get(1)).subtractLocal(t.get(0));
//        tmpB.set(t.get(2)).subtractLocal(t.get(0));
//        tmpA.crossLocal(tmpB);
//        return tmpA.lengthSquared();
//    }
//
//    private static final int getCost(Triangle[] triangles, int index){
//        float leftArea = 0, rightArea = 0;
//        for (int i = 0; i < index; i++){
//            leftArea  += getArea(triangles[i]);
//        }
//        for (int i = index; i < triangles.length; i++){
//            rightArea += getArea(triangles[i]);
//        }
//        float leftCount  = index;
//        float rightCount = triangles.length - index;
//
//        return (int) (1f + 3f * (leftArea * leftCount + rightArea * rightCount));
//    }
//
//    private static final int getOptimalSplitPos(Triangle[] tris, int axis){
//        int bestCost = Integer.MAX_VALUE;
//        int bestPos  = -1;
//        int rate = tris.length / 32;
//        if (tris.length < 64)
//            rate = 1;
//
//        for (int i = 0; i < tris.length; i+=rate){
//            int cost;
//            if ( (cost = getCost(tris, i)) < bestCost){
//                bestCost = cost;
//                bestPos = i;
//            }
//        }
//
//        return bestPos;
//    }

    public void computeFromTris(BIHTriangle[] tris, BoundingBox store) {
        TempVars vars = TempVars.get();
        Vector3f min = vars.vect1.set(new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        Vector3f max = vars.vect2.set(new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));

        Vector3f point;
        for (int i = 0; i < tris.length; i++) {
            point = tris[i].get1();
            BoundingBox.checkMinMax(min, max, point);
            point = tris[i].get2();
            BoundingBox.checkMinMax(min, max, point);
            point = tris[i].get3();
            BoundingBox.checkMinMax(min, max, point);
        }

        Vector3f center = store.getCenter();
        center.set(min.addLocal(max)).multLocal(0.5f);
        store.setXExtent(max.x - center.x);
        store.setYExtent(max.y - center.y);
        store.setZExtent(max.z - center.z);
    }

    public void subdivide(){
        // choose axis and plane split location
        BoundingBox bbox = new BoundingBox();
        computeFromTris(triangles, bbox);

        // choose axis based on longest extent
//        if (bbox.getXExtent() > bbox.getYExtent()){
//            if (bbox.getXExtent() > bbox.getZExtent()){
//                axis = 0;
//            }else{
//                axis = 2;
//            }
//        }else{
//            if (bbox.getYExtent() > bbox.getZExtent()){
//                axis = 1;
//            }else{
//                axis = 2;
//            }
//        }


        float middle;
        // note: axis already set in constructor
        switch (axis){
            case 0: middle = bbox.getCenter().x; break;
            case 1: middle = bbox.getCenter().y; break;
            case 2: middle = bbox.getCenter().z; break;
            default: assert false; return;
        }

        // sort triangles from left to right along axis
        TriangleAxisComparator comparator = comparators[axis];
        Arrays.sort(triangles, comparator);
        int splitIndex = -1;

        leftPlane = triangles[0].getExtreme(axis, false);
        for (int i = 0; i < triangles.length; i++){
            BIHTriangle tri = triangles[i];
            if (splitIndex == -1){
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
                        // first triangle assigned to right
                        rightPlane = tri.getExtreme(axis, true);
                    }
                }else{
                    // triangle assigned to left
                    float ex = tri.getExtreme(axis, false);
                    if (ex > leftPlane)
                        leftPlane = ex;
                }
            }else{
                float ex = tri.getExtreme(axis, true);
                if (ex < rightPlane)
                    rightPlane = ex;
            }
        }

        if (splitIndex < 0){
            splitIndex = triangles.length / 2;
//        splitIndex = getOptimalSplitPos(triangles, axis);
        
        leftPlane = Float.NEGATIVE_INFINITY;
        rightPlane = Float.POSITIVE_INFINITY;

            for (int i = 0; i < splitIndex; i++){
                float ex = triangles[i].getExtreme(axis, false);
                if (ex > leftPlane)
                    leftPlane = ex;
            }
            for (int i = splitIndex; i < triangles.length; i++){
                float ex = triangles[i].getExtreme(axis, true);
                if (ex < rightPlane)
                    rightPlane = ex;
            }
        }


        BIHTriangle[] leftTris = new BIHTriangle[splitIndex];
        System.arraycopy(triangles, 0, leftTris, 0, splitIndex);

        BIHTriangle[] rightTris = new BIHTriangle[triangles.length - splitIndex];
        System.arraycopy(triangles, splitIndex, rightTris, 0, rightTris.length);

        left = new BIHNode(leftTris,   (axis + 1) % 3);
        right = new BIHNode(rightTris, (axis + 1) % 3);

        // no longer a leaf
        triangles = null;
    }

    private static class BIHStackData {

        private final BIHNode node;
        private final float min, max;

        public BIHStackData(BIHNode node, float min, float max) {
            this.node = node;
            this.min = min;
            this.max = max;
        }

    }

    private static final ArrayList<BIHStackData> stack = new ArrayList<BIHStackData>();

    public static long  hits = 0,
                        misses = 0;

    public final void intersectWhere(Ray r, float sceneMin, float sceneMax, int minTrisPerNode,
                                            TrianglePickResults results){
        stack.clear();

        float tHit = Float.POSITIVE_INFINITY;
        float[] origins = { r.getOrigin().x,
                            r.getOrigin().y,
                            r.getOrigin().z };
        float[] invDirections = { 1f / r.getDirection().x,
                                  1f / r.getDirection().y,
                                  1f / r.getDirection().z };

        stack.add(new BIHStackData(this, sceneMin, sceneMax));
        stackloop: while (stack.size() > 0){

            BIHStackData data = stack.remove(stack.size()-1);
            BIHNode node = data.node;
            float tMin = data.min, 
                  tMax = data.max;

            if (tMax < tMin)
                continue;
            
            leafloop: while (node.triangles == null){ // while node is not a leaf
                int a = node.axis;
                
                // find the origin and direction value for the given axis
                float origin = origins[a];
                float invDirection = invDirections[a];

                float tNearSplit, tFarSplit;
                BIHNode nearNode, farNode;

//                if (direction == 0)
//                    direction = FastMath.FLT_EPSILON;

                tNearSplit = (node.leftPlane  - origin) * invDirection;
                tFarSplit  = (node.rightPlane - origin) * invDirection;
                nearNode = node.left;
                farNode  = node.right;

                if (invDirection < 0){
                    float tmpSplit = tNearSplit;
                    tNearSplit = tFarSplit;
                    tFarSplit = tmpSplit;

                    BIHNode tmpNode = nearNode;
                    nearNode = farNode;
                    farNode = tmpNode;
                }
//                if (invDirection >= 0){
//
//                }else{// if (direction < 0){
//                    // if direction on axis is negative,
//                    // switch near split with far split
//                    tNearSplit = (node.rightPlane  - origin) * invDirection;
//                    tFarSplit  = (node.leftPlane   - origin) * invDirection;
//                    nearNode = node.right;
//                    farNode  = node.left;
//                }

                if (tMin > tNearSplit && tMax < tFarSplit){
                    continue stackloop;
                }

                if (tMin > tNearSplit){
                    tMin = max(tMin, tFarSplit);
                    node = farNode;
                }else if (tMax < tFarSplit){
                    tMax = min(tMax, tNearSplit);
                    node = nearNode;
                }else{
                    stack.add(new BIHStackData(farNode,  max(tMin, tFarSplit), tMax));
//                    stack.push(new BIHStackData(nearNode, tMin, min(tMax, tNearSplit)));
//                    continue stackloop;
                    tMax = min(tMax, tNearSplit);
                    node = nearNode;
                }
            }

            if (node.triangles.length > minTrisPerNode){
                // on demand subdivision
                node.subdivide();
                stack.add(new BIHStackData(node, tMin, tMax));
                continue stackloop;
            }

            // a leaf
            for (int i = 0; i < node.triangles.length; i++){
                BIHTriangle tri = node.triangles[i];
                float t = r.intersects(tri.get1(), tri.get2(), tri.get3());
                if (t != Float.POSITIVE_INFINITY)
                    hits ++;
                else
                    misses ++;
                
                if (t < tHit){
                    tHit = t;
                    tMax = min(tMax, tHit);
                    results.addPick(tri.get1(), tri.get2(), tri.get3(), tHit);
                }
            }
//            if (results.size() > 0)
//                return;
        }

//        System.out.println("Hits: "+hits+", misses: "+misses);
//        hits = 0;
//        misses = 0;
    }

}
