package com.jme3.terrain.geomipmap.LodCalc;

import com.jme3.terrain.geomipmap.TerrainPatch;

/**
 *
 * @author bowens
 */
public class LodDistanceCalculatorFactory implements LodCalculatorFactory {

    private float lodThresholdSize = 2f;
    private LodThreshold lodThreshold = null;


    public LodDistanceCalculatorFactory() {

    }
    
    public LodDistanceCalculatorFactory(LodThreshold lodThreshold) {
        this.lodThreshold = lodThreshold;
    }

    
    public LodCalculator createCalculator() {
        return new DistanceLodCalculator();
    }

    public LodCalculator createCalculator(TerrainPatch terrainPatch) {
        if (lodThreshold == null)
            lodThreshold = new SimpleLodThreshold(terrainPatch.getSize(), lodThresholdSize);
        return new DistanceLodCalculator(terrainPatch, lodThreshold);
    }

}
