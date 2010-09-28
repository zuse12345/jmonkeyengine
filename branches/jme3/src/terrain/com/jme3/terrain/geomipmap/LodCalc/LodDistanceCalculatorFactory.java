package com.jme3.terrain.geomipmap.LodCalc;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.terrain.geomipmap.TerrainPatch;
import java.io.IOException;

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

    public void write(JmeExporter ex) throws IOException {
		OutputCapsule c = ex.getCapsule(this);
		c.write(lodThreshold, "lodThreshold", null);
        c.write(lodThresholdSize, "lodThresholdSize", 2);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule c = im.getCapsule(this);
		lodThresholdSize = c.readInt("lodThresholdSize", 2);
        lodThreshold = (LodThreshold) c.readSavable("lodThreshold", null);
    }

}
