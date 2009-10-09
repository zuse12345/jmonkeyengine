package com.g3d.physics;

import com.g3d.scene.Geometry;
import java.util.Set;


/**
 * <code>CollisionResult</code> stores the result of a collision.
 * 
 * @author Lucas Goraieb
 */
public class CollisionResult {

	private Set<Integer> triangles;
	private Geometry geom;
	private StaticCollider collider;
	
	public CollisionResult(StaticCollider collider, Geometry geom, Set<Integer> triangles) {
		this.collider = collider;
		this.geom = geom;
		this.triangles = triangles;
	}

	public Geometry getGeometry() {
		return geom;
	}

	public Set<Integer> getTriangles() {
		return triangles;
	}

	public StaticCollider getCollider() {
		return collider;
	}
	
}
