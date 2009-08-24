package com.g3d.collision.bih;

import com.g3d.bounding.BoundingBox;
import com.g3d.bounding.IntersectionRecord;
import com.g3d.collision.TrianglePickResults;
import com.g3d.math.FastMath;
import com.g3d.math.Ray;
import com.g3d.math.Triangle;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;

import com.g3d.scene.VertexBuffer.Type;
import com.g3d.util.TempVars;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class BIHTree {

    private BIHNode root;
    private Mesh mesh;
    private int maxTrisPerNode;
    private int numTris;
    private float[] pointData;
    private int[] triIndices;

    private static final TriangleAxisComparator[] comparators = new TriangleAxisComparator[3];

    static {
        comparators[0] = new TriangleAxisComparator(0);
        comparators[1] = new TriangleAxisComparator(1);
        comparators[2] = new TriangleAxisComparator(2);
    }

    public BIHTree(Mesh m, int maxTrisPerNode){
        this.mesh = m;
        this.maxTrisPerNode = maxTrisPerNode;
        m.updateCounts();
        m.updateBound();

        FloatBuffer vb = (FloatBuffer) m.getBuffer(Type.Position).getData();
        ShortBuffer ib = (ShortBuffer) m.getBuffer(Type.Index).getData();
       
        numTris = m.getTriangleCount();
        pointData = new float[numTris * 3 * 3];
        int p = 0;
        for (int i = 0; i < numTris*3; i+=3){
            int vert = ib.get(i)*3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);

            vert = ib.get(i+1)*3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);
            
            vert = ib.get(i+2)*3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);
        }

        triIndices = new int[numTris];
        for (int i = 0; i < numTris; i++)
            triIndices[i] = i;
    }

    public BIHTree(Mesh m){
        this(m,21);
    }

    public void construct(){
        BoundingBox sceneBbox = createBox(0, numTris-1);
        root = createNode(0, numTris-1, sceneBbox, 0);
    }

    private BoundingBox createBox(int l, int r) {
        TempVars vars = TempVars.get();
        Vector3f min = vars.vect1.set(new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        Vector3f max = vars.vect2.set(new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
    
        Vector3f v1 = new Vector3f(),
                 v2 = new Vector3f(),
                 v3 = new Vector3f();
        
        for (int i = l; i <= r; i++) {
            getTriangle(i, v1,v2,v3);
            BoundingBox.checkMinMax(min, max, v1);
            BoundingBox.checkMinMax(min, max, v2);
            BoundingBox.checkMinMax(min, max, v3);
        }

        return new BoundingBox(min,max);
    }

    int getTriangleIndex(int triIndex){
        return triIndices[triIndex];
    }

     private int sortTriangles(int l, int r, float split, int axis){
        int pivot = l;
        int j = r;

        Vector3f v1 = new Vector3f(),
                 v2 = new Vector3f(),
                 v3 = new Vector3f();

        while (pivot <= j){
            getTriangle(pivot, v1, v2, v3);
            v1.addLocal(v2).addLocal(v3).multLocal(FastMath.ONE_THIRD);
            if (v1.get(axis) > split){
                swapTriangles(pivot, j);
                --j;
            }else{
                ++pivot;
            }
        }

        pivot = (pivot == l && j < pivot) ? j : pivot;
        return pivot;
    }

    private void setMinMax(BoundingBox bbox, boolean doMin, int axis, float value){
        Vector3f min = bbox.getMin(null);
        Vector3f max = bbox.getMax(null);

        if (doMin)
            min.set(axis, value);
        else
            max.set(axis, value);

        bbox.setMinMax(min, max);
    }

    private float getMinMax(BoundingBox bbox, boolean doMin, int axis){
        if (doMin)
            return bbox.getMin(null).get(axis);
        else
            return bbox.getMax(null).get(axis);
    }

//    private BIHNode createNode2(int l, int r, BoundingBox nodeBbox, int depth){
//        if ((r - l) < maxTrisPerNode || depth > 100)
//            return createLeaf(l, r);
//
//        BoundingBox currentBox = createBox(l, r);
//        int axis = depth % 3;
//        float split = currentBox.getCenter().get(axis);
//
//        TriangleAxisComparator comparator = comparators[axis];
//        Arrays.sort(tris, l, r, comparator);
//        int splitIndex = -1;
//
//        float leftPlane, rightPlane = Float.POSITIVE_INFINITY;
//        leftPlane = tris[l].getExtreme(axis, false);
//        for (int i = l; i <= r; i++){
//            BIHTriangle tri = tris[i];
//            if (splitIndex == -1){
//                float v = tri.getCenter().get(axis);
//                if (v > split){
//                    if (i == 0){
//                        // no left plane
//                        splitIndex = -2;
//                    }else{
//                        splitIndex = i;
//                        // first triangle assigned to right
//                        rightPlane = tri.getExtreme(axis, true);
//                    }
//                }else{
//                    // triangle assigned to left
//                    float ex = tri.getExtreme(axis, false);
//                    if (ex > leftPlane)
//                        leftPlane = ex;
//                }
//            }else{
//                float ex = tri.getExtreme(axis, true);
//                if (ex < rightPlane)
//                    rightPlane = ex;
//            }
//        }
//
//        if (splitIndex < 0){
//            splitIndex = (r - l) / 2;
//
//            leftPlane = Float.NEGATIVE_INFINITY;
//            rightPlane = Float.POSITIVE_INFINITY;
//
//            for (int i = l; i < splitIndex; i++){
//                float ex = tris[i].getExtreme(axis, false);
//                if (ex > leftPlane){
//                    leftPlane = ex;
//                }
//            }
//            for (int i = splitIndex; i <= r; i++){
//                float ex = tris[i].getExtreme(axis, true);
//                if (ex < rightPlane){
//                    rightPlane = ex;
//                }
//            }
//        }
//
//        BIHNode node = new BIHNode(axis);
//        node.leftPlane = leftPlane;
//        node.rightPlane = rightPlane;
//
//        node.leftIndex = l;
//        node.rightIndex = r;
//
//        BoundingBox leftBbox = new BoundingBox(currentBox);
//        setMinMax(leftBbox, false, axis, split);
//        node.left = createNode2(l, splitIndex-1, leftBbox, depth+1);
//
//        BoundingBox rightBbox = new BoundingBox(currentBox);
//        setMinMax(rightBbox, true, axis, split);
//        node.right = createNode2(splitIndex, r, rightBbox, depth+1);
//
//        return node;
//    }

    private BIHNode createNode(int l, int r, BoundingBox nodeBbox, int depth) {
        if ((r - l) < maxTrisPerNode || depth > 100){
            return new BIHNode(l, r);
        }
        
        BoundingBox currentBox = createBox(l, r);

        Vector3f exteriorExt = nodeBbox.getExtent(null);
        Vector3f interiorExt = currentBox.getExtent(null);
        exteriorExt.subtractLocal(interiorExt);

        int axis = 0;
        if (exteriorExt.x > exteriorExt.y){
            if (exteriorExt.x > exteriorExt.z)
                axis = 0;
            else
                axis = 2;
        }else{
            if (exteriorExt.y > exteriorExt.z)
                axis = 1;
            else
                axis = 2;
        }
        if (exteriorExt.equals(Vector3f.ZERO))
            axis = 0;

//        Arrays.sort(tris, l, r, comparators[axis]);
        float split = currentBox.getCenter().get(axis);
        int pivot = sortTriangles(l, r, split, axis);
        if (pivot == l || pivot == r)
            pivot = (r + l) / 2;

        //If one of the partitions is empty, continue with recursion: same level but different bbox
        if (pivot < l){
            //Only right
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);
            return createNode(l, r, rbbox, depth+1);
        }else if (pivot > r){
            //Only left
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);
            return createNode(l, r, lbbox, depth+1);
        }else{
            //Build the node
            BIHNode node = new BIHNode(axis);

            //Left child
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);

            //The left node right border is the plane most right
            node.leftPlane = getMinMax(createBox(l, max(l, pivot - 1)), false, axis);
            node.left = createNode(l, max(l, pivot - 1), lbbox, depth+1); //Recursive call

            //Right Child
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);
            //The right node left border is the plane most left
            node.rightPlane = getMinMax(createBox(pivot, r), true, axis);
            node.right = createNode(pivot, r, rbbox, depth+1); //Recursive call

            return node;
        }
    }

    public void getTriangle(int index, Vector3f v1, Vector3f v2, Vector3f v3){
        int pointIndex = index * 9;

        v1.set(pointData[pointIndex++],
               pointData[pointIndex++],
               pointData[pointIndex++]);

        v2.set(pointData[pointIndex++],
               pointData[pointIndex++],
               pointData[pointIndex++]);
        
        v3.set(pointData[pointIndex++],
               pointData[pointIndex++],
               pointData[pointIndex++]);
    }

    public void swapTriangles(int index1, int index2){
        int p1 = index1 * 9;
        int p2 = index2 * 9;

        // store p1 in tmp
        float[] tmp = new float[9];
        System.arraycopy(pointData, p1, tmp, 0, 9);

        // copy p2 to p1
        System.arraycopy(pointData, p2, pointData, p1, 9);

        // copy tmp to p2
        System.arraycopy(tmp, 0, pointData, p2, 9);

        // swap indices
        int tmp2 = triIndices[index1];
        triIndices[index1] = triIndices[index2];
        triIndices[index2] = tmp2;
    }

    public void intersect(Ray r, float farPlane, Geometry g, TrianglePickResults results){
        results.clear();
        IntersectionRecord ir = mesh.getBound().intersectsWhere(r);
        if (ir.getQuantity() > 0){
            float tMin = ir.getClosestDistance();
            float tMax = ir.getIntersectionDistance(ir.getFarthestPoint());

            tMin = max(tMin, 0);
            tMax = min(tMax, farPlane);
            
            root.intersectWhere(r, this, g, tMin, tMax, results);
            results.finish();
        }
    }

}
