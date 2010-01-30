package com.g3d.scene;

import com.g3d.bounding.BoundingVolume;
import com.g3d.collision.Collidable;
import com.g3d.collision.CollisionResults;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.material.Material;
import com.g3d.math.Matrix4f;
import com.g3d.util.TempVars;
import java.io.IOException;

public class Geometry extends Spatial {

    /**
     * The mesh contained herein
     */
    protected Mesh mesh;

    protected Material material;

    protected transient Matrix4f cachedWorldMat = new Matrix4f();

    /**
     * Do not use this constructor. Serialization purposes only.
     */
    public Geometry(){
    }

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

    public int getVertexCount(){
        return mesh.getVertexCount();
    }

    public int getTriangleCount(){
        return mesh.getTriangleCount();
    }

    public void setMesh(Mesh mesh){
        if (mesh == null)
            throw new NullPointerException();

        this.mesh = mesh;
        setBoundRefresh();
    }

    public Mesh getMesh(){
        return mesh;
    }

    public void setMaterial(Material material){
        this.material = material;
    }

    public Material getMaterial(){
        return material;
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

        assert TempVars.get().lock();
        Matrix4f scaleMat = TempVars.get().tempMat4;
        scaleMat.loadIdentity();
        scaleMat.scale(worldTransform.getScale());
        cachedWorldMat.multLocal(scaleMat);
        assert TempVars.get().unlock();

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

    public int collideWith(Collidable other, CollisionResults results){
        if (refreshFlags != 0)
            throw new IllegalStateException("Scene graph must be updated" +
                                            " before checking collision");

        if (mesh != null){
            // NOTE: BIHTree in mesh already checks collision with the
            // mesh's bound
            return mesh.collideWith(other, cachedWorldMat, worldBound, results);
        }
        return 0;
    }

    @Override
    public Geometry clone(){
        Geometry geomClone = (Geometry) super.clone();
        geomClone.cachedWorldMat = cachedWorldMat.clone();
        if (material != null)
                geomClone.material = material.clone();
        return geomClone;
    }

    @Override
    public void write(G3DExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(mesh, "mesh", null);
        oc.write(material, "material", null);
    }

    @Override
    public void read(G3DImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        mesh = (Mesh) ic.readSavable("mesh", null);
        material = (Material) ic.readSavable("material", null);
    }

}
