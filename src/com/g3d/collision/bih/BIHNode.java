package com.g3d.collision.bih;

import com.g3d.bounding.BoundingBox;
import com.g3d.collision.TrianglePickResults;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.math.Ray;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * Bounding Interval Hierarchy.
 * Based on:
 *
 * Instant Ray Tracing: The Bounding Interval Hierarchy
 * By Carsten Wächter and Alexander Keller
 */
public class BIHNode implements Savable {

    int leftIndex, rightIndex;

    BIHNode left;
    BIHNode right;
    float leftPlane;
    float rightPlane;
    int axis;

    public BIHNode(int l, int r){
        leftIndex = l;
        rightIndex = r;
        axis = 3; // indicates leaf
    }

    public BIHNode(int axis){
        this.axis = axis;
    }

    public BIHNode(){
    }

    /*
     * int leftIndex, rightIndex;

    BIHNode left;
    BIHNode right;
    float leftPlane;
    float rightPlane;
    int axis;*/

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(leftIndex,  "left_index", 0);
        oc.write(rightIndex, "right_index", 0);
        oc.write(leftPlane, "left_plane", 0);
        oc.write(rightPlane, "right_plane", 0);
        oc.write(axis, "axis", 0);
        oc.write(left, "left_node", null);
        oc.write(right, "right_node", null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        leftIndex = ic.readInt("left_index", 0);
        rightIndex = ic.readInt("right_index", 0);
        leftPlane = ic.readFloat("left_plane", 0);
        rightPlane = ic.readFloat("right_plane", 0);
        axis = ic.readInt("axis", 0);
        left = (BIHNode) ic.readSavable("left_node", null);
        right = (BIHNode) ic.readSavable("right_node", null);
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

    public final void intersectWhere(BoundingBox box, BIHTree tree, Geometry g, int minTrisPerNode, TrianglePickResults results){
        stack.clear();

        float[] minExts  = { box.getCenter().x - box.getXExtent(),
                             box.getCenter().y - box.getYExtent(),
                             box.getCenter().z - box.getZExtent() };

        float[] maxExts  = { box.getCenter().x + box.getXExtent(),
                             box.getCenter().y + box.getYExtent(),
                             box.getCenter().z + box.getZExtent() };

        stack.add(new BIHStackData(this, 0,0));

        Vector3f v1 = new Vector3f(),
                 v2 = new Vector3f(),
                 v3 = new Vector3f();

        stackloop: while (stack.size() > 0){
            BIHNode node = stack.remove(stack.size()-1).node;

            while (node.axis != 2){
                int a = node.axis;

                float maxExt = minExts[a];
                float minExt = maxExts[a];

                boolean intersectLeft = minExt < node.leftPlane &&
                                        node.leftPlane < maxExt;
                boolean intersectRight = minExt < node.rightPlane &&
                                        node.rightPlane < maxExt;

                if (intersectLeft && intersectRight){
                    stack.add(new BIHStackData(node.right, 0, 0));
                    node = node.left;
                }else if (intersectLeft){
                    node = node.left;
                }else if (intersectRight){
                    node = node.right;
                }else{
                    continue stackloop;
                }
            }

            for (int i = node.leftIndex; i <= node.rightIndex; i++){
                tree.getTriangle(i, v1,v2,v3);
                if (box.intersects(v1,v2,v3)){
                    results.addPick(g, tree.getTriangleIndex(i), 0);
                }
            }
        }
    }

    public final void intersectWhere(Ray r, BIHTree tree, Geometry g, float sceneMin, float sceneMax,
                                            TrianglePickResults results){
        stack.clear();

        float tHit = Float.POSITIVE_INFINITY;
        float[] origins = { r.getOrigin().x,
                            r.getOrigin().y,
                            r.getOrigin().z };
        float[] invDirections = { 1f / r.getDirection().x,
                                  1f / r.getDirection().y,
                                  1f / r.getDirection().z };

        Vector3f v1 = new Vector3f(),
                 v2 = new Vector3f(),
                 v3 = new Vector3f();

        stack.add(new BIHStackData(this, sceneMin, sceneMax));
        stackloop: while (stack.size() > 0){

            BIHStackData data = stack.remove(stack.size()-1);
            BIHNode node = data.node;
            float tMin = data.min, 
                  tMax = data.max;

            if (tMax < tMin)
                continue;
            
            leafloop: while (node.axis != 3){ // while node is not a leaf
                int a = node.axis;
                
                // find the origin and direction value for the given axis
                float origin = origins[a];
                float invDirection = invDirections[a];

                float tNearSplit, tFarSplit;
                BIHNode nearNode, farNode;

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
                    tMax = min(tMax, tNearSplit);
                    node = nearNode;
                }
            }

//            if ( (node.rightIndex - node.leftIndex) > minTrisPerNode){
//                // on demand subdivision
//                node.subdivide();
//                stack.add(new BIHStackData(node, tMin, tMax));
//                continue stackloop;
//            }

            // a leaf
            for (int i = node.leftIndex; i <= node.rightIndex; i++){
                tree.getTriangle(i, v1,v2,v3);
                float t = r.intersects(v1,v2,v3);
                if (t < tHit){
                    tHit = t;
                    tMax = min(tMax, tHit);
                    results.addPick(g, tree.getTriangleIndex(i), tHit);
                }
            }
//            if (results.size() > 0)
//                return;
        }
    }

}
