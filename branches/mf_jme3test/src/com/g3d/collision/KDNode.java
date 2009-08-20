package com.g3d.collision;

import com.g3d.math.Plane;
import com.g3d.math.Vector3f;
import java.util.Arrays;
import java.util.Comparator;

public class KDNode {

    protected final Vector3f location;
    protected final int axis;
    protected final KDNode left;
    protected final KDNode right;

    private static class VectorAxisComparator implements Comparator<Vector3f> {

        private final int axis;

        public VectorAxisComparator(int axis){
            this.axis = axis;
        }

        @Override
        public int compare(Vector3f o1, Vector3f o2) {
            float v1, v2;
            switch (axis){
                case 0: v1 = o1.x; v2 = o2.x; break;
                case 1: v1 = o1.y; v2 = o2.y; break;
                case 2: v1 = o1.z; v2 = o2.z; break;
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

    /**
     * Used for computing the median point based on axis
     */
    private final static VectorAxisComparator[] comparators = new VectorAxisComparator[3];
    private final static Vector3f tmpA = new Vector3f(),
                                  tmpB = new Vector3f();

    static {
        // set comparators for X, Y and Z axes
        comparators[0] = new VectorAxisComparator(0);
        comparators[1] = new VectorAxisComparator(1);
        comparators[2] = new VectorAxisComparator(2);
    }

    public KDNode(Vector3f location, int axis, KDNode left, KDNode right) {
        this.location = location;
        this.axis = axis;
        this.left = left;
        this.right = right;
    }

    private static final int getExtreme(Vector3f o1, Vector3f o2, Vector3f o3, int axis, boolean left){
        float v1, v2, v3;
        switch (axis){
            case 0: v1 = o1.x; v2 = o2.x; v3 = o3.x; break;
            case 1: v1 = o1.y; v2 = o2.y; v3 = o3.y; break;
            case 2: v1 = o1.z; v2 = o2.z; v3 = o3.z; break;
            default: assert false; return -1;
        }
        if (left){
            if (v1 < v3){ // v3 is greatest
                if (v1 < v2) // v1 is smallest
                    return 0;
                else // v1 >= v2, v2 is smallest
                    return 1;
            }else{ // v1 is greatest
                if (v3 < v2) // v3 is smallest
                    return 2;
                else // v3 >= v2, v2 is smallest
                    return 1;
            }
        }else{
            if (v1 > v3){ // v3 is greatest
                if (v1 > v2) // v1 is smallest
                    return 0;
                else // v1 >= v2, v2 is smallest
                    return 1;
            }else{ // v1 is greatest
                if (v3 > v2) // v3 is smallest
                    return 2;
                else // v3 >= v2, v2 is smallest
                    return 1;
            }
        }
    }

    private static final float getArea(Vector3f v1, Vector3f v2, Vector3f v3){
        tmpA.set(v2).subtractLocal(v1);
        tmpB.set(v3).subtractLocal(v1);
        tmpA.crossLocal(tmpB);
        return tmpA.length() / 2.0f;
    }

    public static void main(String[] args){
        Vector3f[] ar = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(1,0,0),
            new Vector3f(0,1,0),

            new Vector3f(0,0,0),
            new Vector3f(1,0,0),
            new Vector3f(0,1,0),

            new Vector3f(0,0,0),
            new Vector3f(1,0,0),
            new Vector3f(0,1,0),

            new Vector3f(0,0,0),
            new Vector3f(1,0,0),
            new Vector3f(0,1,0),
        };
        System.out.println(getOptimalSplitPos(ar, 0));
    }

    private static final int getCost(Vector3f[] points, int index){
        float leftArea = 0, rightArea = 0;
        for (int i = 0; i < index; i += 3){
            leftArea  += getArea(points[i], points[i+1], points[i+2]);
        }
        for (int i = index; i < points.length; i += 3){
            rightArea += getArea(points[i], points[i+1], points[i+2]);
        }
        float leftCount = index / 3;
        float rightCount = (points.length - index) / 3;
        
        return (int) (1f + 3f * (leftArea * leftCount + rightArea * rightCount));
    }

    private static final int getOptimalSplitPos(Vector3f[] points, int axis){
        int bestCost = 1000000;
        int bestPos  = -1;

        for (int i = 0; i < points.length; i += 3){
            int leftExtreme = i + getExtreme(points[i],
                                         points[i+1],
                                         points[i+2],
                                         axis,
                                         true);
            int rightExtreme = i + getExtreme(points[i],
                                          points[i+1],
                                          points[i+2],
                                          axis,
                                          false);
            int cost;
            if ( (cost = getCost(points, leftExtreme)) < bestCost){
                bestCost = cost;
                bestPos = leftExtreme;
            }
            if ( (cost = getCost(points, rightExtreme)) < bestCost){
                bestCost = cost;
                bestPos = rightExtreme;
            }
        }

        return bestPos;
    }

    private static final KDNode createKDTree(Vector3f[] points, int depth){
        if (points == null || points.length == 0)
            return null;

        if (points.length == 1)
            return new KDNode(points[0], 0, null, null);

        int axis = depth % 2;
        VectorAxisComparator comparator = comparators[axis];
        Arrays.sort(points, comparator);
        int median = points.length / 2;

        Vector3f[] leftPts = new Vector3f[median];
        System.arraycopy(points, 0, leftPts, 0, leftPts.length);
        KDNode left = createKDTree(leftPts, depth + 1);
        leftPts = null;
        
        Vector3f[] rightPts = new Vector3f[points.length - median - 1];
        System.arraycopy(points, median + 1, rightPts, 0, rightPts.length);
        KDNode right = createKDTree(rightPts, depth + 1);
        rightPts = null;
        
        return new KDNode(points[median], axis, left, right);

    }

    public static final KDNode createKDTree(Vector3f[] points){
        Vector3f[] copy = new Vector3f[points.length];
        System.arraycopy(points, 0, copy, 0, points.length);
        return createKDTree(copy, 0);
    }

    public KDNode getLeft() {
        return left;
    }

    public Vector3f getLocation() {
        return location;
    }

    public KDNode getRight() {
        return right;
    }

}
