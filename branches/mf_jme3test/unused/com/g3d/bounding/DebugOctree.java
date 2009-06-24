package com.g3d.bounding;

import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.shape.Box;
import g3dtest.OctreeDebugger;
import java.util.List;

/**
 * <code>DebugOctree</code> provides an Octree implementation with Debug Helpers.<br>
 * Requires OctreeDebugger class.
 *
 * @author Lucas Goraieb
 * @version $Id: Octree.java,v 0.5 2007/04/27 20:33:02 nca Exp $
 */
public class DebugOctree extends Octree {

	private OctreeDebugger debug = OctreeDebugger.getInstance();

	private Geometry debugBox;

	public DebugOctree(Geometry geom, int trianglePerNode) {
        super(geom, trianglePerNode);
		debugBox = debug.addBox(getBoundingBox());
	}

	private DebugOctree(DebugOctree parent, BoundingBox bb, List<Integer> tris) {
		super(parent, bb, tris);
		System.out.println("Node created. BoundingBox extent: "+getBoundingBox().xExtent);
		debugBox = debug.addBox(getBoundingBox());
	}

	@Override
	public void onSubdivide() {
		System.out.println("Subdividing triangles: "+getTriangleData().size());
	}

	@Override
	public void onRecurseFinished() {
		System.out.println("Recurse finished. Triangles: "+getTriangleData().size());
	}

	@Override
	public void onIntersectStart() {
		debug.reset();
	}

	@Override
	public void onIntersectHit() {
		debug.hit(this);
	}

	@Override
	protected Octree newOctreeNode(BoundingBox bb, List<Integer> tris) {
		return new DebugOctree(this, bb, tris);
	}

	public Geometry getDebugBox() {
		return debugBox;
	}

	public List<Integer> getTriangleData() {
		return super.getTriangleData();
	}
}
