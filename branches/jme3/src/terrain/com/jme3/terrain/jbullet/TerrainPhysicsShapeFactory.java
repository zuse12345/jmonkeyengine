package com.jme3.terrain.jbullet;

import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;

/**
 * Create a node populated with the collision heightfield of the Terrain tree.
 * Terrain will always have a mass of 0,meaning it cannot move.
 * 
 * @author Brent Owens
 */
public class TerrainPhysicsShapeFactory {

	/**
	 * Creates the actual collision node.
	 * 
	 * @param terrain to create the collision heightfield from
	 * @return the node with the physics node
	 */
	public Node createPhysicsMesh(TerrainQuad terrain) {

		PhysicsNode n = new PhysicsNode(new HeightfieldCollisionShape(terrain.getHeightMap(), terrain.getLocalScale()), 0 );
		return n;


		//BELOW: the old way, keeping it here for a little bit as a reference (and so it gets into version control so I can always access it)
		/*Map<TerrainPatch,Vector3f> all = new HashMap<TerrainPatch,Vector3f>();
		terrain.getAllTerrainPatchesWithTranslation(all, terrain.getLocalTranslation());

		Node node = new Node();

		for (Entry<TerrainPatch,Vector3f> entry : all.entrySet()) {
			TerrainPatch tp = entry.getKey();
			Vector3f trans = entry.getValue();
			PhysicsNode n = new PhysicsNode(new HeightfieldCollisionShape(tp.getHeightmap(), trans, tp.getLocalScale()), 0 );
			n.setLocalTranslation(trans);
			node.attachChild(n);
		}*/
		
	}
}
