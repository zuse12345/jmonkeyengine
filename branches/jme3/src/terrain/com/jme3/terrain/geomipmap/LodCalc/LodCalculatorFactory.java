package com.jme3.terrain.geomipmap.LodCalc;

import com.jme3.export.Savable;
import com.jme3.terrain.geomipmap.TerrainPatch;

/**
 * Creates LOD Calculator objects for the terrain patches.
 *
 * @author Brent Owens
 */
public interface LodCalculatorFactory extends Savable {

    public LodCalculator createCalculator();
    public LodCalculator createCalculator(TerrainPatch terrainPatch);
}
