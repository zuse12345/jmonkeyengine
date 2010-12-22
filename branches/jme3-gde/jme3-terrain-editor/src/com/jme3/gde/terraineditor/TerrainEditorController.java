/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.terraineditor;

import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.Terrain;
import java.io.IOException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Modifies the actual terrain in the scene.
 * 
 * @author normenhansen, bowens
 */
public class TerrainEditorController {
    private JmeSpatial jmeRootNode;
    private Node terrainNode;
    private Node rootNode;
    private TerrainToolController toolController;
    private DataObject currentFileObject;


    public TerrainEditorController(JmeSpatial jmeRootNode, DataObject currentFileObject) {
        this.jmeRootNode = jmeRootNode;
        rootNode = this.jmeRootNode.getLookup().lookup(Node.class);
        this.currentFileObject = currentFileObject;
    }

    public void setToolController(TerrainToolController toolController) {
        this.toolController = toolController;
    }

    public FileObject getCurrentFileObject() {
        return currentFileObject.getPrimaryFile();
    }

    public DataObject getCurrentDataObject() {
        return currentFileObject;
    }

    public void setNeedsSave(boolean state) {
        currentFileObject.setModified(state);
    }

    public boolean isNeedSave() {
        return currentFileObject.isModified();
    }

    public void saveScene() {
        try {
            currentFileObject.getLookup().lookup(SaveCookie.class).save();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected Node getTerrain(Spatial root) {
        if (terrainNode != null)
            return terrainNode;

        if (root == null)
            root = rootNode;

        // is this the terrain?
        if (root instanceof Terrain && root instanceof Node) {
            terrainNode = (Node)root;
            return terrainNode;
        }

        if (root instanceof Node) {
            Node n = (Node) root;
            for (Spatial c : n.getChildren()) {
                if (c instanceof Node){
                    Node res = getTerrain(c);
                    if (res != null)
                        return res;
                }
            }
        }

        return null;
    }

    /**
     * Perform the actual height modification on the terrain.
     * @param worldLoc the location in the world where the tool was activated
     * @param radius of the tool, terrain in this radius will be affected
     * @param heightFactor the amount to adjust the height by
     */
    protected void doModifyTerrainHeight(Vector3f worldLoc, float radius, float heightFactor) {

        Terrain terrain = (Terrain) getTerrain(null);
        if (terrain == null)
            return;

        setNeedsSave(true);

        float posX = worldLoc.x - radius;
        float posZ = worldLoc.z - radius;

        // offset it by radius because in the loop we iterate through 2 radii
        int radiusStepsX = (int) (radius / ((Node)terrain).getLocalScale().x);
        int radiusStepsZ = (int) (radius / ((Node)terrain).getLocalScale().z);

        float xStepAmount = ((Node)terrain).getLocalScale().x;
        float zStepAmount = ((Node)terrain).getLocalScale().z;

        for (int z=0; z<radiusStepsZ*2; z++) {
			for (int x=0; x<radiusStepsX*2; x++) {
				
				if (isInRadius(x-radius,z-radius,radius)) {
                    // see if it is in the radius of the tool
					float height = calculateHeight(radius, heightFactor, x-radius, z-radius);

                    float locX = posX + (x*xStepAmount);
                    float locZ = posZ + (z*zStepAmount);

					// increase the height
					terrain.adjustHeight(new Vector2f(locX, locZ), height);
				}
			}
		}

        ((Node)terrain).updateModelBound(); // or else we won't collide with it where we just edited
        
    }

    /**
	 * See if the X,Y coordinate is in the radius of the circle. It is assumed
	 * that the "grid" being tested is located at 0,0 and its dimensions are 2*radius.
	 * @param x
	 * @param z
	 * @param radius
	 * @return
	 */
	private boolean isInRadius(float x, float y, float radius) {
		Vector2f point = new Vector2f(x,y);
		// return true if the distance is less than equal to the radius
		return Math.abs(point.length()) <= radius;
	}

    /**
     * Interpolate the height value based on its distance from the center (how far along
     * the radius it is).
     * The farther from the center, the less the height will be.
     * This produces a linear height falloff.
     * @param radius of the tool
     * @param heightFactor potential height value to be adjusted
     * @param x location
     * @param z location
     * @return the adjusted height value
     */
    private float calculateHeight(float radius, float heightFactor, float x, float z) {
        // find percentage for each 'unit' in radius
        Vector2f point = new Vector2f(x,z);
        float val = Math.abs(point.length()) / radius;
        val = 1 - val;
        return heightFactor * val;
	}


    public void cleanup(){

    }

    public void doCleanup(){
    }

}
