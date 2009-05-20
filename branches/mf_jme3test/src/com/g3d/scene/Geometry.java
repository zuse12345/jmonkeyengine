package com.g3d.scene;

import com.g3d.bounding.BoundingVolume;
import com.g3d.light.LightList;
import com.g3d.math.Matrix4f;
import com.g3d.util.TempVars;

public class Geometry extends Spatial {

    /**
     * The mesh contained herein
     */
    protected Mesh mesh;

    protected Matrix4f cachedWorldMat = new Matrix4f();

    /**
     * Create a geometry node without any mesh data.
     * @param name The name of this geometry
     */
    public Geometry(String name){
        super(name);
    }

    /**
     * Create a geometry node with mesh data.
     *
     * @param name The name of this geometry
     * @param mesh The mesh data for this geometry
     */
    public Geometry(String name, Mesh mesh){
        this(name);
        if (mesh == null)
            throw new NullPointerException();

        this.mesh = mesh;
    }

    public Mesh getMesh(){
        return mesh;
    }

    /**
     * @return The bounding volume of the mesh, in model space.
     */
    public BoundingVolume getModelBound(){
        return mesh.getBound();
    }

    /**
     * Updates the bounding volume of the mesh. Should be called when the
     * mesh has been modified.
     */
    public void updateModelBound() {
        mesh.updateBound();
        setBoundRefresh();
    }

    /**
     * <code>updateWorldBound</code> updates the bounding volume that contains
     * this geometry. The location of the geometry is based on the location of
     * all this node's parents.
     *
     * @see com.jme.scene.Spatial#updateWorldBound()
     */
    @Override
    protected void updateWorldBound() {
        super.updateWorldBound();
        if (mesh.getBound() != null) {
            worldBound = mesh.getBound().transform(worldTransform, worldBound);
        }
    }

    @Override
    protected void updateWorldTransforms(){
        super.updateWorldTransforms();

        cachedWorldMat.loadIdentity();

        cachedWorldMat.setRotationQuaternion(worldTransform.getRotation());
        cachedWorldMat.setTranslation(worldTransform.getTranslation());

        Matrix4f scaleMat = TempVars.get().tempMat4;
        scaleMat.loadIdentity();
        scaleMat.scale(worldTransform.getScale());
        cachedWorldMat.multLocal(scaleMat);

        // geometry requires lights to be sorted
        worldLights.sort(true);
    }

    public Matrix4f getWorldMatrix(){
        return cachedWorldMat;
    }

    @Override
    public void setModelBound(BoundingVolume modelBound) {
        this.worldBound = null;
        mesh.setBound(modelBound);
        updateModelBound();
    }

}
