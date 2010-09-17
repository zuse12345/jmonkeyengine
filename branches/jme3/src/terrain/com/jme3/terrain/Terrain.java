package com.jme3.terrain;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.List;

public interface Terrain {

	/**
	 * Get the height of the terrain at the specified X-Z coorindate.
	 * @param xz the X-Z coordinate
	 * @return
	 */
	public float getHeight(Vector2f xz);
	
	/**
	 * Set the height at the specified X-Z coordinate.
	 * @param xzCoordinate coordinate to set the height
	 * @param height that will be set at the coordinate
	 */
	public void setHeight(Vector2f xzCoordinate, float height);
	
	/**
	 * Tell the terrain system to use/not use Level of Detail algorithms.
	 */
	public void useLOD(boolean useLod);
	
	public boolean isUsingLOD();
	
	/**
	 * This is calculated by the specific LOD algorithm.
	 * A value of one means that the terrain is showing full detail.
	 * The higher the value, the more the terrain has been generalized 
	 * and the less detailed it will be.
	 */
	public int getMaxLod();
	
	/**
	 * Called in the update (pre or post, up to you) method of your game.
	 * Calculates the level of detail of the terrain and adjusts its geometry.
	 * This is where the Terrain's LOD algorithm will change the detail of 
	 * the terrain based on how far away this position is from the particular
	 * terrain patch.
	 * @param location often the Camera's location
	 */
	public void update(List<Vector3f> location);
	
}
