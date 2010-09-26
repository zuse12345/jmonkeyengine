package com.jme3.terrain.geomipmap.LodCalc;

import com.jme3.export.Savable;


/**
 * Calculates the LOD value based on where the camera is.
 * This is plugged into the Terrain system and any terrain
 * using LOD will use this to determine when a patch of the 
 * terrain should switch Levels of Detail.
 * 
 * @author bowens
 */
public interface LodThreshold extends Savable {

	/**
	 * A distance of how far between each LOD threshold.
	 */
	public float getLodDistanceThreshold();
}
