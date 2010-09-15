package com.jme3.terrain.geomipmap;

import com.jme3.terrain.LodThreshold;

/**
 * Just multiplies the terrain patch size by 2. So every two
 * patches away the camera is, the LOD changes.
 * 
 * Set it higher to have the LOD change less frequently.
 * 
 * @author bowens
 */
public class SimpleLodThreshold implements LodThreshold {
	
	private int size; // size of a terrain patch
	private int LOD_MULTIPLIER = 2;
	
	public SimpleLodThreshold(int size) {
		this.size = size;
	}

	@Override
	public float getLodDistanceThreshold() {
		return size*LOD_MULTIPLIER;
	}

}
