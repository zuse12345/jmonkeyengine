package com.jme3.terrain.geomipmap;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import java.util.List;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.terrain.Terrain;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Tells the terrain to update its Level of Detail.
 * It needs the cameras to do this, and there could possibly
 * be several cameras in the scene, so it accepts a list
 * of cameras.
 * NOTE: right now it just uses the first camera passed in,
 * in the future it will use all of them to determine what
 * LOD to set.
 * 
 * @author Brent Owens
 */
public class TerrainLodControl extends AbstractControl {
	
	private Terrain terrain;
	private List<Camera> cameras;


    public TerrainLodControl() {

    }
    
	/**
	 * Only uses the first camera right now.
	 * @param terrain to act upon (must me a Spatial)
	 * @param cameras one or more cameras to reference for LOD calc
	 */
	public TerrainLodControl(Terrain terrain, List<Camera> cameras) {
		super((Spatial)terrain);
		this.terrain = terrain;
		this.cameras = cameras;
	}
	
	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		
	}

	@Override
	protected void controlUpdate(float tpf) {
		//list of cameras for when terrain supports multiple cameras (ie split screen)
        List<Vector3f> cameraLocations = new ArrayList<Vector3f>();
        if (cameras != null) {
            for (Camera c : cameras)
                cameraLocations.add(c.getLocation());
            terrain.update(cameraLocations);
        }
	}

	public Control cloneForSpatial(Spatial spatial) {
		if (spatial instanceof Terrain)
			return new TerrainLodControl((Terrain)spatial, cameras);
		
		return null;
	}


    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }


}
