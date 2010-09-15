package com.jme3.terrain.geomipmap;



import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.LodThreshold;

/**
 * A terrain quad is a node in the quad tree of the terrain system.
 * The root terrain quad will be the only one that receives the update() call every frame
 * and it will determine if there has been any LOD change.
 * 
 * The leaves of the terrain quad tree are Terrain Patches. These have the real geometry mesh.
 * 
 * @author Brent Owens
 */
public class TerrainQuad extends Node {

	protected Vector2f offset;

	protected int totalSize;

	protected int size;

	protected Vector3f stepScale;

	protected float offsetAmount;

	protected short quadrant = 1;
	
	protected LodThreshold lodThresholdCalculator;
	
	protected Vector3f lastCameraLocation; // used for LOD calc
	private boolean lodCalcRunning = false;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		           public Thread newThread(Runnable r) {
		             Thread th = new Thread(r);
		             th.setDaemon(true);
		             return th;
		           }
		    });
	
	private HashMap<String,UpdatedTerrainPatch> updatedPatches;
	private Object updatePatchesLock = new Object();
	
	public TerrainQuad() {
		super("Terrain");
	}
	
	public TerrainQuad(String name, int blockSize, int size, Vector3f stepScale, float[] heightMap) {
		this(name, blockSize, size, stepScale, heightMap, size, new Vector2f(), 0, new SimpleLodThreshold(blockSize));
	}
	
	public TerrainQuad(String name, int blockSize, int size, Vector3f stepScale, float[] heightMap, LodThreshold lodThresholdCalculator) {
		this(name, blockSize, size, stepScale, heightMap, size, new Vector2f(), 0, lodThresholdCalculator);
	}
	
	 protected TerrainQuad(String name, int blockSize, int size,
				Vector3f stepScale, float[] heightMap, int totalSize,
				Vector2f offset, float offsetAmount,
				LodThreshold lodThresholdCalculator)
	 {
		super(name);
		if (!FastMath.isPowerOfTwo(size - 1)) {
			throw new RuntimeException("size given: " + size + "  Terrain quad sizes may only be (2^N + 1)");
		}
		
		if (heightMap == null)
			heightMap = generateDefaultHeightMap(size);

		this.offset = offset;
		this.offsetAmount = offsetAmount;
		this.totalSize = totalSize;
		this.size = size;
		this.stepScale = stepScale;
		this.lodThresholdCalculator = lodThresholdCalculator;
		split(blockSize, heightMap);
		
		//fixNormals();
	}
	 
	public void setLodThreshold(LodThreshold thresholdCalculator) {
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).setLodThreshold(thresholdCalculator);
				} else if (child instanceof TerrainPatch) {
					((TerrainPatch) child).setLodThreshold(thresholdCalculator);
				}
			}
		}
	}
	 
	
	/**
	 * Create just a flat heightmap
	 */
	private float[] generateDefaultHeightMap(int size) {
		float[] heightMap = new float[size*size];
		
		return heightMap;
	}

	 /**
	  * Call from the update() method of your gamestate to update
	  * the LOD values of each patch.
	  * This will perform the geometry calculation in a background thread and 
	  * do the actual update on the opengl thread.
	  */
	public void update(Vector3f location) {
		
		// update any existing ones that need updating
		updateQuadLODs();
		
		if (lastCameraLocation != null) {
			if (lastCameraLocation.equals(location))
				return; // don't update if in same spot
			else
				lastCameraLocation = location.clone();
		}
		else {
			lastCameraLocation = location.clone();
			return;
		}
		
		if (isLodCalcRunning()) {
			return;
		}
		
		if (getParent() instanceof TerrainQuad) {
			return; // we just want the root quad to perform this.
		}
		
		UpdateLOD updateLodThread = new UpdateLOD(location);
		executor.execute(updateLodThread);
		
	}
	
	private synchronized boolean isLodCalcRunning() {
		return lodCalcRunning;
	}
	
	private synchronized void setLodCalcRunning(boolean running) {
		lodCalcRunning = running;
	}
	
	/**
	 * Calculates the LOD of all child terrain patches.
	 */
	private class UpdateLOD implements Runnable {
		private Vector3f camLocation;
		
		UpdateLOD(Vector3f location) {
			camLocation = location;
		}
		
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			if (isLodCalcRunning()) {
				//System.out.println("thread already running");
				return;
			}
			//System.out.println("spawned thread "+toString());
			setLodCalcRunning(true);
			
			// go through each patch and calculate its LOD based on camera distance
			HashMap<String,UpdatedTerrainPatch> updated = new HashMap<String,UpdatedTerrainPatch>();
			boolean lodChanged = calculateLod(camLocation, updated); // 'updated' gets populated here
			
			if (!lodChanged) {
				// not worth updating anything else since no one's LOD changed
				setLodCalcRunning(false);
				return;
			}
			// then calculate its neighbour LOD values for seaming in the shader
			findNeighboursLod(updated);
			
			fixEdges(updated); // 'updated' can get added to here
			
			reIndexPages(updated);
			
			setUpdateQuadLODs(updated); // set back to main ogl thread
			
			setLodCalcRunning(false);
			//double duration = (System.currentTimeMillis()-start);
			//System.out.println("terminated in "+duration);
		}

		
		
	}
	
	private void setUpdateQuadLODs(HashMap<String,UpdatedTerrainPatch> updated) {
		synchronized (updatePatchesLock) {
			updatedPatches = updated;
		}
	}
	
	/**
	 * Back on the ogl thread: update the terrain patch geometries
	 * @param updatedPatches to be updated
	 */
	private void updateQuadLODs() {
		synchronized (updatePatchesLock) {
			//if (true)
			//	return;
			if (updatedPatches == null || updatedPatches.size() == 0)
				return;
			
			//TODO do the actual geometry update here
			for (UpdatedTerrainPatch utp : updatedPatches.values()) {
				utp.updateAll();
			}
			
			updatedPatches.clear();
		}
	}
	
	protected boolean calculateLod(Vector3f location, HashMap<String,UpdatedTerrainPatch> updates) {
		
		boolean lodChanged = false;
		
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					boolean b = ((TerrainQuad) child).calculateLod(location, updates);
					if (b)
						lodChanged = true;
				} else if (child instanceof TerrainPatch) {
					boolean b = ((TerrainPatch) child).calculateLod(location, updates);
					if (b)
						lodChanged = true;
				}
			}
		}
		
		return lodChanged;
	}
	
	protected synchronized void findNeighboursLod(HashMap<String,UpdatedTerrainPatch> updated) {
		if (children != null) {
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).findNeighboursLod(updated);
				} else if (child instanceof TerrainPatch) {
					TerrainPatch patch = (TerrainPatch) child;
					TerrainPatch right = findRightPatch(patch);
					TerrainPatch down = findDownPatch(patch);
					
					UpdatedTerrainPatch utp = updated.get(patch.getName());
					if (utp == null) {
						utp = new UpdatedTerrainPatch(patch, patch.lod);
						updated.put(utp.getName(), utp);
					}
					
					if (right != null) {
						UpdatedTerrainPatch utpR = updated.get(right.getName());
						if (utpR == null) {
							utpR = new UpdatedTerrainPatch(right, right.lod);
							updated.put(utpR.getName(), utpR);
						}
						
						utp.setRightLod(utpR.getNewLod());
						utpR.setLeftLod(utp.getNewLod());
						//patch.lodRight = right.lod;
						//right.lodLeft = patch.lod;
					}
					if (down != null) {
						UpdatedTerrainPatch utpD = updated.get(down.getName());
						if (utpD == null) {
							utpD = new UpdatedTerrainPatch(down, down.lod);
							updated.put(utpD.getName(), utpD);
						}
						
						utp.setBottomLod(utpD.getNewLod());
						utpD.setTopLod(utp.getNewLod());
						//patch.lodBottom = down.lod;
						//down.lodTop = patch.lod;
					}
					/*
					patch.lodRight = right != null ?  right.lod : patch.lod;
					
					patch.lodBottom = down != null ? down.lod : patch.lod;
					*/
					/*patch.lodLeft = left != null ? 
						 patch.lod;
					if (down != null)
						down.lodTop = patch.lod;
					*/
				}
			}
		}
	}
	
	/**
	 * Find any neighbours that should have their edges seamed because another neighbour
	 * changed its LOD to a greater value (less detailed)
	 */
	protected synchronized void fixEdges(HashMap<String,UpdatedTerrainPatch> updated) {
		if (children != null) {
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).fixEdges(updated);
				} else if (child instanceof TerrainPatch) {
					TerrainPatch patch = (TerrainPatch) child;
					UpdatedTerrainPatch utp = updated.get(patch.getName());
					
					if(utp.lodChanged()) {
						TerrainPatch right = findRightPatch(patch);
						TerrainPatch down = findDownPatch(patch);
						TerrainPatch top = findTopPatch(patch);
						TerrainPatch left = findLeftPatch(patch);
						if (right != null) {
							UpdatedTerrainPatch utpR = updated.get(right.getName());
							if (utpR == null) {
								utpR = new UpdatedTerrainPatch(right, right.lod);
								updated.put(utpR.getName(), utpR);
							}
							utpR.setFixEdges(true);
						}
						if (down != null) {
							UpdatedTerrainPatch utpD = updated.get(down.getName());
							if (utpD == null) {
								utpD = new UpdatedTerrainPatch(down, down.lod);
								updated.put(utpD.getName(), utpD);
							}
							utpD.setFixEdges(true);
						}
						if (top != null){
							UpdatedTerrainPatch utpT = updated.get(top.getName());
							if (utpT == null) {
								utpT = new UpdatedTerrainPatch(top, top.lod);
								updated.put(utpT.getName(), utpT);
							}
							utpT.setFixEdges(true);
						}
						if (left != null){
							UpdatedTerrainPatch utpL = updated.get(left.getName());
							if (utpL == null) {
								utpL = new UpdatedTerrainPatch(left, left.lod);
								updated.put(utpL.getName(), utpL);
							}
							utpL.setFixEdges(true);
						}
					}
				}
			}
		}
	}
	
	protected synchronized void reIndexPages(HashMap<String,UpdatedTerrainPatch> updated) {
		if (children != null) {
			for (int i = children.size(); --i >= 0;) {
				Spatial child = children.get(i);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).reIndexPages(updated);
				} else if (child instanceof TerrainPatch) {
					((TerrainPatch) child).reIndexGeometry(updated);
				}
			}
		}
	}

	/**
	 * <code>split</code> divides the heightmap data for four children. The
	 * children are either pages or blocks. This is dependent on the size of the
	 * children. If the child's size is less than or equal to the set block
	 * size, then blocks are created, otherwise, pages are created.
	 * 
	 * @param blockSize
	 *			the blocks size to test against.
	 * @param heightMap
	 *			the height data.
	 */
	protected void split(int blockSize, float[] heightMap) {
		if ((size >> 1) + 1 <= blockSize) {
			createQuadPatch(heightMap);
		} else {
			createQuad(blockSize, heightMap);
		}

	}

	/**
	 * <code>createQuadPage</code> generates four new pages from this page.
	 */
	protected void createQuad(int blockSize, float[] heightMap) {
		// create 4 terrain pages
		int quarterSize = size >> 2;

		int split = (size + 1) >> 1;

		Vector2f tempOffset = new Vector2f();
		offsetAmount += quarterSize;

		// 1 upper left
		float[] heightBlock1 = createHeightSubBlock(heightMap, 0, 0, split);

		Vector3f origin1 = new Vector3f(-quarterSize * stepScale.x, 0,
				-quarterSize * stepScale.z);

		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin1.x;
		tempOffset.y += origin1.z;

		TerrainQuad page1 = new TerrainQuad(getName() + "Quad1", blockSize,
				split, stepScale, heightBlock1, totalSize, tempOffset,
				offsetAmount, lodThresholdCalculator);
		page1.setLocalTranslation(origin1);
		page1.quadrant = 1;
		this.attachChild(page1);

		// 2 lower left
		float[] heightBlock2 = createHeightSubBlock(heightMap, 0, split - 1,
				split);

		Vector3f origin2 = new Vector3f(-quarterSize * stepScale.x, 0,
				quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin2.x;
		tempOffset.y += origin2.z;

		TerrainQuad page2 = new TerrainQuad(getName() + "Quad2", blockSize,
				split, stepScale, heightBlock2, totalSize, tempOffset,
				offsetAmount, lodThresholdCalculator);
		page2.setLocalTranslation(origin2);
		page2.quadrant = 2;
		this.attachChild(page2);

		// 3 upper right
		float[] heightBlock3 = createHeightSubBlock(heightMap, split - 1, 0,
				split);

		Vector3f origin3 = new Vector3f(quarterSize * stepScale.x, 0,
				-quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin3.x;
		tempOffset.y += origin3.z;

		TerrainQuad page3 = new TerrainQuad(getName() + "Quad3", blockSize,
				split, stepScale, heightBlock3, totalSize, tempOffset,
				offsetAmount, lodThresholdCalculator);
		page3.setLocalTranslation(origin3);
		page3.quadrant = 3;
		this.attachChild(page3);
		// //
		// 4 lower right
		float[] heightBlock4 = createHeightSubBlock(heightMap, split - 1,
				split - 1, split);

		Vector3f origin4 = new Vector3f(quarterSize * stepScale.x, 0,
				quarterSize * stepScale.z);

		tempOffset = new Vector2f();
		tempOffset.x = offset.x;
		tempOffset.y = offset.y;
		tempOffset.x += origin4.x;
		tempOffset.y += origin4.z;

		TerrainQuad page4 = new TerrainQuad(getName() + "Quad4", blockSize,
				split, stepScale, heightBlock4, totalSize, tempOffset,
				offsetAmount, lodThresholdCalculator);
		page4.setLocalTranslation(origin4);
		page4.quadrant = 4;
		this.attachChild(page4);

	}

	/**
	 * <code>createQuadBlock</code> creates four child blocks from this page.
	 */
	protected void createQuadPatch(float[] heightMap) {
		// create 4 terrain blocks
		int quarterSize = size >> 2;
		int halfSize = size >> 1;
		int split = (size + 1) >> 1;

		
		offsetAmount += quarterSize;

		// 1 upper left
		float[] heightBlock1 = createHeightSubBlock(heightMap, 0, 0, split);

		Vector3f origin1 = new Vector3f(-halfSize * stepScale.x, 0, -halfSize
				* stepScale.z);

		Vector2f tempOffset1 = new Vector2f();
		tempOffset1.x = offset.x;
		tempOffset1.y = offset.y;
		tempOffset1.x += origin1.x / 2;
		tempOffset1.y += origin1.z / 2;

		TerrainPatch block1 = new TerrainPatch(getName() + "Patch1", split,
				stepScale, heightBlock1, origin1, totalSize, tempOffset1,
				offsetAmount);
		block1.setQuadrant((short) 1);
		this.attachChild(block1);
		block1.setModelBound(new BoundingBox());
		block1.updateModelBound();
		block1.setLodThreshold(lodThresholdCalculator);

		// 2 lower left
		float[] heightBlock2 = createHeightSubBlock(heightMap, 0, split - 1,
				split);

		Vector3f origin2 = new Vector3f(-halfSize * stepScale.x, 0, 0);

		Vector2f tempOffset2 = new Vector2f();
		tempOffset2.x = offset.x;
		tempOffset2.y = offset.y;
		tempOffset2.x += origin1.x / 2;
		tempOffset2.y += quarterSize * stepScale.z;

		TerrainPatch block2 = new TerrainPatch(getName() + "Patch2", split,
				stepScale, heightBlock2, origin2, totalSize, tempOffset2,
				offsetAmount);
		block2.setQuadrant((short) 2);
		this.attachChild(block2);
		block2.setModelBound(new BoundingBox());
		block2.updateModelBound();
		block2.setLodThreshold(lodThresholdCalculator);

		// 3 upper right
		float[] heightBlock3 = createHeightSubBlock(heightMap, split - 1, 0,
				split);

		Vector3f origin3 = new Vector3f(0, 0, -halfSize * stepScale.z);

		Vector2f tempOffset3 = new Vector2f();
		tempOffset3.x = offset.x;
		tempOffset3.y = offset.y;
		tempOffset3.x += quarterSize * stepScale.x;
		tempOffset3.y += origin3.z / 2;

		TerrainPatch block3 = new TerrainPatch(getName() + "Patch3", split,
				stepScale, heightBlock3, origin3, totalSize, tempOffset3,
				offsetAmount);
		block3.setQuadrant((short) 3);
		this.attachChild(block3);
		block3.setModelBound(new BoundingBox());
		block3.updateModelBound();
		block3.setLodThreshold(lodThresholdCalculator);

		// 4 lower right
		float[] heightBlock4 = createHeightSubBlock(heightMap, split - 1,
				split - 1, split);

		Vector3f origin4 = new Vector3f(0, 0, 0);

		Vector2f tempOffset4 = new Vector2f();
		tempOffset4.x = offset.x;
		tempOffset4.y = offset.y;
		tempOffset4.x += quarterSize * stepScale.x;
		tempOffset4.y += quarterSize * stepScale.z;

		TerrainPatch block4 = new TerrainPatch(getName() + "Patch4", split,
				stepScale, heightBlock4, origin4, totalSize, tempOffset4,
				offsetAmount);
		block4.setQuadrant((short) 4);
		this.attachChild(block4);
		block4.setModelBound(new BoundingBox());
		block4.updateModelBound();
		block4.setLodThreshold(lodThresholdCalculator);
	}
	
	public static final float[] createHeightSubBlock(float[] heightMap, int x,
			int y, int side) {
		float[] rVal = new float[side * side];
		int bsize = (int) FastMath.sqrt(heightMap.length);
		int count = 0;
		for (int i = y; i < side + y; i++) {
			for (int j = x; j < side + x; j++) {
				if (j < bsize && i < bsize)
					rVal[count] = heightMap[j + (i * bsize)];
				count++;
			}
		}
		return rVal;
	}
	
	 
	public void setModelBound(BoundingVolume v) {
		for (int i = 0; i < this.getQuantity(); i++) {
			if (this.getChild(i) instanceof TerrainQuad) {
				((TerrainQuad) getChild(i)).setModelBound(v.clone(null));
			} else if (this.getChild(i) instanceof TerrainPatch) {
				((TerrainPatch) getChild(i)).setModelBound(v.clone(null));

			}
		}
	}
	 
	public void updateModelBound() {
		for (int i = 0; i < this.getQuantity(); i++) {
			if (this.getChild(i) instanceof TerrainQuad) {
				((TerrainQuad) getChild(i)).updateModelBound();
			} else if (this.getChild(i) instanceof TerrainPatch) {
				((TerrainPatch) getChild(i)).updateModelBound();

			}
		}
	}
	
	public float getHeight(float x, float z) {
		// determine which quadrant this is in.
		Spatial child = null;
		int split = (size - 1) >> 1;
		float halfmapx = split * stepScale.x, halfmapz = split * stepScale.z;
		float newX = 0, newZ = 0;
		if (x == 0)
			x += .001f;
		if (z == 0)
			z += .001f;
		if (x > 0) {
			if (z > 0) {
				// upper right
				child = getChild(3);
				newX = x;
				newZ = z;
			} else {
				// lower right
				child = getChild(2);
				newX = x;
				newZ = z + halfmapz;
			}
		} else {
			if (z > 0) {
				// upper left
				child = getChild(1);
				newX = x + halfmapx;
				newZ = z;
			} else {
				// lower left...
				child = getChild(0);
				if (x == 0)
					x -= .1f;
				if (z == 0)
					z -= .1f;
				newX = x + halfmapx;
				newZ = z + halfmapz;
			}
		}
		if (child instanceof TerrainPatch)
			return ((TerrainPatch) child).getHeight(newX, newZ);
		else if (child instanceof TerrainQuad)
			return ((TerrainQuad) child).getHeight(x
					- ((TerrainQuad) child).getLocalTranslation().x, z
					- ((TerrainQuad) child).getLocalTranslation().z);
		return Float.NaN;
	}
	
	
	public short getQuadrant() {
		return quadrant;
	}

	public void setQuadrant(short quadrant) {
		this.quadrant = quadrant;
	}
	

	protected TerrainPatch getPatch(int quad) {
		if (children != null)
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainPatch) {
					TerrainPatch tb = (TerrainPatch) child;
					if (tb.getQuadrant() == quad)
						return tb;
				}
			}
		return null;
	}

	protected TerrainQuad getQuad(int quad) {
		if (children != null)
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					TerrainQuad tq = (TerrainQuad) child;
					if (tq.getQuadrant() == quad)
						return tq;
				}
			}
		return null;
	}

	protected TerrainPatch findRightPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 1)
			return getPatch(3);
		else if (tp.getQuadrant() == 2)
			return getPatch(4);
		else if (tp.getQuadrant() == 3) {
			// find the page to the right and ask it for child 1.
			TerrainQuad quad = findRightQuad();
			if (quad != null)
				return quad.getPatch(1);
		} else if (tp.getQuadrant() == 4) {
			// find the page to the right and ask it for child 2.
			TerrainQuad quad = findRightQuad();
			if (quad != null)
				return quad.getPatch(2);
		}

		return null;
	}

	protected TerrainPatch findDownPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 1)
			return getPatch(2);
		else if (tp.getQuadrant() == 3)
			return getPatch(4);
		else if (tp.getQuadrant() == 2) {
			// find the page below and ask it for child 1.
			TerrainQuad quad = findDownQuad();
			if (quad != null)
				return quad.getPatch(1);
		} else if (tp.getQuadrant() == 4) {
			TerrainQuad quad = findDownQuad();
			if (quad != null)
				return quad.getPatch(3);
		}

		return null;
	}
	
	
	protected TerrainPatch findTopPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 2)
			return getPatch(1);
		else if (tp.getQuadrant() == 4)
			return getPatch(3);
		else if (tp.getQuadrant() == 1) {
			// find the page above and ask it for child 2.
			TerrainQuad quad = findTopQuad();
			if (quad != null)
				return quad.getPatch(2);
		} else if (tp.getQuadrant() == 3) {
			TerrainQuad quad = findTopQuad();
			if (quad != null)
				return quad.getPatch(4);
		}

		return null;
	}
	
	protected TerrainPatch findLeftPatch(TerrainPatch tp) {
		if (tp.getQuadrant() == 3)
			return getPatch(1);
		else if (tp.getQuadrant() == 4)
			return getPatch(2);
		else if (tp.getQuadrant() == 1) {
			// find the page above and ask it for child 2.
			TerrainQuad quad = findLeftQuad();
			if (quad != null)
				return quad.getPatch(3);
		} else if (tp.getQuadrant() == 2) {
			TerrainQuad quad = findLeftQuad();
			if (quad != null)
				return quad.getPatch(4);
		}

		return null;
	}

	protected TerrainQuad findRightQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 1)
			return pQuad.getQuad(3);
		else if (quadrant == 2)
			return pQuad.getQuad(4);
		else if (quadrant == 3) {
			TerrainQuad quad = pQuad.findRightQuad();
			if (quad != null)
				return quad.getQuad(1);
		} else if (quadrant == 4) {
			TerrainQuad quad = pQuad.findRightQuad();
			if (quad != null)
				return quad.getQuad(2);
		}

		return null;
	}

	protected TerrainQuad findDownQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 1)
			return pQuad.getQuad(2);
		else if (quadrant == 3)
			return pQuad.getQuad(4);
		else if (quadrant == 2) {
			TerrainQuad quad = pQuad.findDownQuad();
			if (quad != null)
				return quad.getQuad(1);
		} else if (quadrant == 4) {
			TerrainQuad quad = pQuad.findDownQuad();
			if (quad != null)
				return quad.getQuad(3);
		}

		return null;
	}
	
	protected TerrainQuad findTopQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 2)
			return pQuad.getQuad(1);
		else if (quadrant == 4)
			return pQuad.getQuad(3);
		else if (quadrant == 1) {
			TerrainQuad quad = pQuad.findTopQuad();
			if (quad != null)
				return quad.getQuad(2);
		} else if (quadrant == 3) {
			TerrainQuad quad = pQuad.findTopQuad();
			if (quad != null)
				return quad.getQuad(4);
		}

		return null;
	}
	
	protected TerrainQuad findLeftQuad() {
		if (getParent() == null || !(getParent() instanceof TerrainQuad))
			return null;

		TerrainQuad pQuad = (TerrainQuad) getParent();

		if (quadrant == 3)
			return pQuad.getQuad(1);
		else if (quadrant == 4)
			return pQuad.getQuad(2);
		else if (quadrant == 1) {
			TerrainQuad quad = pQuad.findLeftQuad();
			if (quad != null)
				return quad.getQuad(3);
		} else if (quadrant == 2) {
			TerrainQuad quad = pQuad.findLeftQuad();
			if (quad != null)
				return quad.getQuad(4);
		}

		return null;
	}
	
	/**
	 * Ignoring the normals for now. The lighting just makes the terrain "pop" noticeably.
	 * Use a lightmap instead.
	 */
	public void fixNormals() {
	/*	if (children != null) {
			for (int x = children.size(); --x >= 0;) {
				Spatial child = children.get(x);
				if (child instanceof TerrainQuad) {
					((TerrainQuad) child).fixNormals();
				} else if (child instanceof TerrainPatch) {
					TerrainPatch tb = (TerrainPatch) child;
					TerrainPatch right = findRightPatch(tb);
					TerrainPatch down = findDownPatch(tb);
					int tbSize = tb.getSize();
					if (right != null) {
						float[] normData = new float[3];
						for (int y = 0; y < tbSize; y++) {
							int index1 = ((y + 1) * tbSize) - 1;
							int index2 = (y * tbSize);
							right.getNormalBuffer().position(index2 * 3);
							right.getNormalBuffer().get(normData);
							tb.getNormalBuffer().position(index1 * 3);
							tb.getNormalBuffer().put(normData);
						}
						deleteNormalVBO(right);

					}
					if (down != null) {
						int rowStart = ((tbSize - 1) * tbSize);
						float[] normData = new float[3];
						for (int z = 0; z < tbSize; z++) {
							int index1 = rowStart + z;
							int index2 = z;
							down.getNormalBuffer().position(index2 * 3);
							down.getNormalBuffer().get(normData);
							tb.getNormalBuffer().position(index1 * 3);
							tb.getNormalBuffer().put(normData);
						}
						deleteNormalVBO(down);
					}
					deleteNormalVBO(tb);
				}
			}
		}
		*/
	}
	
	
	@Override
	public void read(JmeImporter e) throws IOException {
		super.read(e);
		InputCapsule c = e.getCapsule(this);
		size = c.readInt("size", 0);
		stepScale = (Vector3f) c.readSavable("stepScale", null);
		offset = (Vector2f) c.readSavable("offset", new Vector2f(0,0));
		offsetAmount = c.readInt("offsetAmount", 0);
		quadrant = c.readShort("quadrant", (short) 0);
	}

	@Override
	public void write(JmeExporter e) throws IOException {
		super.write(e);
		OutputCapsule c = e.getCapsule(this);
		c.write(size, "size", 0);
		c.write(stepScale, "stepScale", null);
		c.write(offset, "offset", new Vector2f(0,0));
		c.write(offsetAmount, "offsetAmount", 0);
		c.write(quadrant, "quadrant", 0);
	}
	
	@Override
    public Node clone(){
		//TODO use importer and exporter to clone it
		return null;
	}
}

