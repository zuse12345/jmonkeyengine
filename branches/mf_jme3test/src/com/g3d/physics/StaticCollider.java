package com.g3d.physics;

import com.g3d.bounding.BoundingVolume;
import com.g3d.collision.TrianglePickResults;
import com.g3d.collision.bih.BIHTree;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>StaticCollider</code> provides an implementation of a Collider to be used for static elements.<br>
 * Like walls, maps, terrains etc.
 * 
 * @author Lucas Goraieb
 */
public class StaticCollider extends Collider {

    private PhysicMaterial physicMaterial = new PhysicMaterial();
    private BIHTree bihTree;
    private TrianglePickResults results;

    /**
     * Creates a static collider using a TriMesh as source.
     * @param mesh TriMesh source
     * @param trianglesPerNode Max number of triangles in one octree node. Depends on the mesh's size and geometry.
     */
    public StaticCollider(Geometry mesh, int trianglesPerNode) {
        this.spatial = mesh;
        bihTree = new BIHTree(mesh, trianglesPerNode);
    }

    /**
     * Build the octrees. This is called automatically by the CollisionScene.build() method.
     */
    public void build() {
        bihTree.construct();
    }

    protected void intersect(BoundingVolume bv, List<CollisionResult> collisionResult) {
        bihTree.intersectWhere(bv, results);
        if (results.size() > 0) {
            Set<Integer> indices = new HashSet<Integer>();
            for (int i = 0; i < results.size(); i++){
                indices.add(results.getPick(i).getIndex());
            }
            collisionResult.add(
                    new CollisionResult(this,
                                        results.getClosestPick().getGeometry(),
                                        indices));
        }
    }

    public PhysicMaterial getPhysicMaterial() {
        return physicMaterial;
    }

    /**
     * Get the collider's Mesh
     * @return Collider's mesh
     */
    public Geometry getGeometry() {
        return (Geometry) spatial;
    }

    @Override
    protected Vector3f scaleFromSpace(Vector3f vector) {
        return vector;
    }

    @Override
    protected Vector3f scaleToSpace(Vector3f vector) {
        return vector;
    }
}
