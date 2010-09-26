
package com.jme3.terrain.geomipmap.LodCalc;

import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.terrain.geomipmap.UpdatedTerrainPatch;
import java.util.HashMap;
import java.util.List;

/**
 * Calculate the Level of Detail of a terrain patch based on the
 * cameras, or other locations.
 *
 * @author Brent Owens
 */
public interface LodCalculator extends Savable {

    public void setTerrainPatch(TerrainPatch terrainPatch);
    public boolean calculateLod(List<Vector3f> locations, HashMap<String,UpdatedTerrainPatch> updates);
}
