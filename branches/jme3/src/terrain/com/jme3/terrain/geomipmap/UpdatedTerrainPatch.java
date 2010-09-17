package com.jme3.terrain.geomipmap;

import java.nio.IntBuffer;

import com.jme3.scene.VertexBuffer.Type;

/**
 * Stores a terrain patch's details so the LOD background thread can update
 * the actual terrain patch back on the ogl thread.
 * 
 * @author Brent Owens
 *
 */
public class UpdatedTerrainPatch {
	
	private TerrainPatch updatedPatch;
	private int newLod;
	private int previousLod;
	private int rightLod,topLod,leftLod,bottomLod;
	private IntBuffer newIndexBuffer;
	private boolean reIndexNeeded = false;
	private boolean fixEdges = false;
	
	public UpdatedTerrainPatch(TerrainPatch updatedPatch, int newLod) {
		this.updatedPatch = updatedPatch;
		this.newLod = newLod;
	}
	
	public UpdatedTerrainPatch(TerrainPatch updatedPatch, int newLod, int prevLOD, boolean reIndexNeeded) {
		this.updatedPatch = updatedPatch;
		this.newLod = newLod;
		this.previousLod = prevLOD;
		this.reIndexNeeded = reIndexNeeded;
		
	}

	public String getName() {
		return updatedPatch.getName();
	}
	
	protected boolean lodChanged() {
		if (reIndexNeeded && previousLod != newLod)
			return true;
		else
			return false;
	}
	
	protected TerrainPatch getUpdatedPatch() {
		return updatedPatch;
	}

	protected void setUpdatedPatch(TerrainPatch updatedPatch) {
		this.updatedPatch = updatedPatch;
	}

	protected int getNewLod() {
		return newLod;
	}

	protected void setNewLod(int newLod) {
		this.newLod = newLod;
	}

	protected IntBuffer getNewIndexBuffer() {
		return newIndexBuffer;
	}

	protected void setNewIndexBuffer(IntBuffer newIndexBuffer) {
		this.newIndexBuffer = newIndexBuffer;
	}


	protected int getRightLod() {
		return rightLod;
	}


	protected void setRightLod(int rightLod) {
		this.rightLod = rightLod;
	}


	protected int getTopLod() {
		return topLod;
	}


	protected void setTopLod(int topLod) {
		this.topLod = topLod;
	}


	protected int getLeftLod() {
		return leftLod;
	}


	protected void setLeftLod(int leftLod) {
		this.leftLod = leftLod;
	}


	protected int getBottomLod() {
		return bottomLod;
	}


	protected void setBottomLod(int bottomLod) {
		this.bottomLod = bottomLod;
	}

	public boolean isReIndexNeeded() {
		return reIndexNeeded;
	}

	public void setReIndexNeeded(boolean reIndexNeeded) {
		this.reIndexNeeded = reIndexNeeded;
	}

	public boolean isFixEdges() {
		return fixEdges;
	}

	public void setFixEdges(boolean fixEdges) {
		this.fixEdges = fixEdges;
	}

	public int getPreviousLod() {
		return previousLod;
	}

	public void setPreviousLod(int previousLod) {
		this.previousLod = previousLod;
	}

	public void updateAll() {
		updatedPatch.setLod(newLod);
		updatedPatch.setLodRight(rightLod);
		updatedPatch.setLodTop(topLod);
		updatedPatch.setLodLeft(leftLod);
		updatedPatch.setLodBottom(bottomLod);
		if (reIndexNeeded || fixEdges) {
			updatedPatch.setPreviousLod(previousLod);
			updatedPatch.getMesh().clearBuffer(Type.Index);
			updatedPatch.getMesh().setBuffer(Type.Index, 3, newIndexBuffer);
		}
	}

}
