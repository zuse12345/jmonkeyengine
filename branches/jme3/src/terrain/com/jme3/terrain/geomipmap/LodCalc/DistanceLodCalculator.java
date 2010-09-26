/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.terrain.geomipmap.LodCalc;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.terrain.geomipmap.UpdatedTerrainPatch;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Calculates the LOD of the terrain based on its distance from the
 * cameras. Taking the minimum distance from all cameras.
 *
 * @author bowens
 */
public class DistanceLodCalculator implements LodCalculator {

    private TerrainPatch terrainPatch;
    private LodThreshold lodThresholdCalculator;

    public DistanceLodCalculator() {
    }

    public DistanceLodCalculator(LodThreshold lodThresholdCalculator) {
        this.lodThresholdCalculator = lodThresholdCalculator;
    }

    public DistanceLodCalculator(TerrainPatch terrainPatch, LodThreshold lodThresholdCalculator) {
        this.terrainPatch = terrainPatch;
        this.lodThresholdCalculator = lodThresholdCalculator;
    }

    public boolean calculateLod(List<Vector3f> locations, HashMap<String, UpdatedTerrainPatch> updates) {
        float distance = getCenterLocation().distance(locations.get(0));

		// go through each lod level to find the one we are in
		for (int i=0; i<=terrainPatch.getMaxLod(); i++) {
			if (distance < lodThresholdCalculator.getLodDistanceThreshold()*(i+1)) {
				boolean reIndexNeeded = false;
				if (i != terrainPatch.getLod()) {
					reIndexNeeded = true;
					//System.out.println("lod change: "+lod+" > "+i+"    dist: "+distance);
				}
				int prevLOD = terrainPatch.getLod();
				//previousLod = lod;
				//lod = i;
				UpdatedTerrainPatch utp = updates.get(terrainPatch.getName());
				if (utp == null) {
					utp = new UpdatedTerrainPatch(terrainPatch, i);//save in here, do not update actual variables
					updates.put(utp.getName(), utp);
				}
				utp.setPreviousLod(prevLOD);
				utp.setReIndexNeeded(reIndexNeeded);
				return reIndexNeeded;
			}
		}

		int newLOD = terrainPatch.getLod();
		int prevLOD = terrainPatch.getPreviousLod();

		if (newLOD != terrainPatch.getMaxLod())
			prevLOD = newLOD;

		// max lod (least detailed)
		newLOD = terrainPatch.getMaxLod();

		boolean reIndexNeeded = false;

		if (prevLOD != newLOD)
			reIndexNeeded = true;

		UpdatedTerrainPatch utp = updates.get(terrainPatch.getName());
		if (utp == null) {
			utp = new UpdatedTerrainPatch(terrainPatch, newLOD);// save in here, do not update actual variables
			updates.put(utp.getName(), utp);
		}
		utp.setPreviousLod(prevLOD);
		utp.setReIndexNeeded(reIndexNeeded);

		return reIndexNeeded;
    }

    public Vector3f getCenterLocation() {
		Vector3f loc = terrainPatch.getWorldTranslation().clone();
		loc.x += terrainPatch.getSize()/2;
		loc.z += terrainPatch.getSize()/2;
		return loc;
	}

    public void setTerrainPatch(TerrainPatch terrainPatch) {
        this.terrainPatch = terrainPatch;
    }

    protected LodThreshold getLodThreshold() {
        return lodThresholdCalculator;
    }

    protected void setLodThreshold(LodThreshold lodThresholdCalculator) {
        this.lodThresholdCalculator = lodThresholdCalculator;
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(lodThresholdCalculator, "lodThresholdCalculator", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        lodThresholdCalculator = (LodThreshold) ic.readSavable("lodThresholdCalculator", null);
    }

}
