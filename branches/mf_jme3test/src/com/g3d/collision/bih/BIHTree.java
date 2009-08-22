package com.g3d.collision.bih;

import com.g3d.bounding.IntersectionRecord;
import com.g3d.collision.TrianglePickResults;
import com.g3d.math.Ray;
import com.g3d.math.Vector3f;
import com.g3d.scene.Mesh;

public class BIHTree {

    private BIHNode root;
    private Mesh mesh;
    private int minTrisPerNode = 21;

    public BIHTree(Mesh m, int minTrisPerNode){
        this.mesh = m;
        this.minTrisPerNode = minTrisPerNode;
        m.updateCounts();
        m.updateBound();

        int triN = m.getTriangleCount();
        BIHTriangle[] tris = new BIHTriangle[triN];

        Vector3f v1 = new Vector3f(),
                 v2 = new Vector3f(),
                 v3 = new Vector3f();

        for (int i = 0; i < triN; i++){
            m.getTriangle(i, v1, v2, v3);
            tris[i] = new BIHTriangle(v1, v2, v3);
        }

        root = new BIHNode(tris, 0);
    }

    public BIHTree(Mesh m){
        this(m,21);
    }

    private void subdivide(BIHNode node){
        if (node.triangles.length > minTrisPerNode){
            node.subdivide();
            subdivide(node.left);
            subdivide(node.right);
        }
    }

    public void construct(){
        subdivide(root);
    }

    public void intersect(Ray r, TrianglePickResults results){
        results.clear();
        IntersectionRecord ir = mesh.getBound().intersectsWhere(r);
        if (ir.getQuantity() > 0){
            float tMin = ir.getClosestDistance();
            float tMax = ir.getIntersectionDistance(ir.getFarthestPoint());
            root.intersectWhere(r, tMin, tMax, minTrisPerNode, results);
            results.finish();
        }
    }

}
