/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.scene;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.material.Material;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.ControlType;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.util.ArrayList;


/**
 * <code>Spatial</code> defines the base class for scene graph nodes. It
 * maintains a link to a parent, it's local transforms and the world's
 * transforms. All other nodes, such as <code>Node</code> and
 * <code>Geometry</code> are subclasses of <code>Spatial</code>.
 *
 * @author Mark Powell
 * @author Joshua Slack
 * @version $Revision: 4075 $, $Data$
 */
public abstract class Spatial implements Savable, Cloneable, Collidable {

    public enum CullHint {
        /** 
         * Do whatever our parent does. If no parent, we'll default to dynamic.
         */
        Inherit,

        /**
         * Do not draw if we are not at least partially within the view frustum
         * of the renderer's camera.
         */
        Dynamic,

        /** 
         * Always cull this from view.
         */
        Always,

        /**
         * Never cull this from view. Note that we will still get culled if our
         * parent is culled.
         */
        Never;
    }

    /**
     * Refresh flag types
     */
    protected static final int RF_TRANSFORM = 0x01, // need light resort + combine transforms
                               RF_BOUND = 0x02,
                               RF_LIGHTLIST = 0x04; // changes in light lists

    protected CullHint cullHint = CullHint.Inherit;

    /** 
     * Spatial's bounding volume relative to the world.
     */
    protected BoundingVolume worldBound;

    /**
     * LightList
     */
    protected LightList localLights = new LightList(this);

    protected LightList worldLights = new LightList(this);

    /** 
     * This spatial's name.
     */
    protected String name;

    // scale values
    protected transient Camera.FrustumIntersect frustrumIntersects = Camera.FrustumIntersect.Intersects;

    protected RenderQueue.Bucket queueBucket = RenderQueue.Bucket.Inherit;

    protected ShadowMode shadowMode = RenderQueue.ShadowMode.Inherit;

    public transient float queueDistance = Float.NEGATIVE_INFINITY;

    protected Transform localTransform;

    protected Transform worldTransform;

    protected IntMap<Control> controls = new IntMap<Control>();

    /** 
     * Spatial's parent, or null if it has none.
     */
    protected transient Node parent;

    /**
     * Refresh flags. Indicate what data of the spatial need to be
     * updated to reflect the correct state.
     */
    protected transient int refreshFlags = 0;

    /**
     * Default Constructor.
     */
    public Spatial() {
        localTransform = new Transform();
        worldTransform = new Transform();
        refreshFlags |= RF_BOUND;
    }

    /**
     * Constructor instantiates a new <code>Spatial</code> object setting the
     * rotation, translation and scale value to defaults.
     *
     * @param name
     *            the name of the scene element. This is required for
     *            identification and comparision purposes.
     */
    public Spatial(String name) {
        this();
        this.name = name;
    }

    /**
     * Indicate that the transform of this spatial has changed and that
     * a refresh is required.
     */
    protected void setTransformRefresh(){
        refreshFlags |= RF_TRANSFORM;
        setBoundRefresh();
    }

    protected void setLightListRefresh(){
        refreshFlags |= RF_LIGHTLIST;
    }

    /**
     * Indicate that the bounding of this spatial has changed and that
     * a refresh is required.
     */
    protected void setBoundRefresh(){
        refreshFlags |= RF_BOUND;

        // XXX: Replace with a recursive call?
        Spatial p = parent;
        while (p != null){
            if ((p.refreshFlags & RF_BOUND) != 0)
                return;

            p.refreshFlags |= RF_BOUND;
            p = p.parent;
        }
    }

    /**
     * <code>checkCulling</code> checks the spatial with the camera to see if it
     * should be culled.
     * <p>
     * This method is called by the renderer. Usually it should not be called
     * directly.
     *
     * @param cam The camera to check against.
     * @return true if inside or intersecting camera frustum
     * (should be rendered), false if outside.
     */
    public boolean checkCulling(Camera cam){
        CullHint cm = getCullHint();
        if (cm == Spatial.CullHint.Always){
            setLastFrustumIntersection(Camera.FrustumIntersect.Outside);
            return false;
        } else if (cm == Spatial.CullHint.Never){
            setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
            return true;
        }

        int state = cam.getPlaneState();

        // check to see if we can cull this node
        frustrumIntersects = (parent != null ? parent.frustrumIntersects
                : Camera.FrustumIntersect.Intersects);

        if (cm == Spatial.CullHint.Dynamic
                && frustrumIntersects == Camera.FrustumIntersect.Intersects) {
            frustrumIntersects = cam.contains(getWorldBound());
        }

        cam.setPlaneState(state);
        return frustrumIntersects != Camera.FrustumIntersect.Outside;
    }

    /**
     * Sets the name of this spatial.
     *
     * @param name
     *            The spatial's new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this spatial.
     *
     * @return This spatial's name.
     */
    public String getName() {
        return name;
    }

    public LightList getWorldLightList() {
        return worldLights;
    }

    /**
     * <code>getWorldRotation</code> retrieves the absolute rotation of the
     * Spatial.
     *
     * @return the Spatial's world rotation matrix.
     */
    public Quaternion getWorldRotation() {
        return worldTransform.getRotation();
    }

    /**
     * <code>getWorldTranslation</code> retrieves the absolute translation of
     * the spatial.
     *
     * @return the world's tranlsation vector.
     */
    public Vector3f getWorldTranslation() {
        return worldTransform.getTranslation();
    }

    /**
     * <code>getWorldScale</code> retrieves the absolute scale factor of the
     * spatial.
     *
     * @return the world's scale factor.
     */
    public Vector3f getWorldScale() {
        return worldTransform.getScale();
    }

    public Transform getWorldTransform(){
        return worldTransform;
    }

    /**
     * <code>rotateUpTo</code> is a util function that alters the
     * localrotation to point the Y axis in the direction given by newUp.
     *
     * @param newUp
     *            the up vector to use - assumed to be a unit vector.
     */
    public void rotateUpTo(Vector3f newUp) {
        TempVars vars = TempVars.get();
        assert vars.lock();

        Vector3f compVecA = vars.vect1;
        Quaternion q = vars.quat1;

        // First figure out the current up vector.
        Vector3f upY = compVecA.set(Vector3f.UNIT_Y);
        Quaternion rot = localTransform.getRotation();
        rot.multLocal(upY);

        // get angle between vectors
        float angle = upY.angleBetween(newUp);

        // figure out rotation axis by taking cross product
        Vector3f rotAxis = upY.crossLocal(newUp).normalizeLocal();

        // Build a rotation quat and apply current local rotation.
        q.fromAngleNormalAxis(angle, rotAxis);
        q.mult(rot, rot);

        assert vars.unlock();

        setTransformRefresh();
    }

    /**
     * <code>lookAt</code> is a convienence method for auto-setting the local
     * rotation based on a position and an up vector. It computes the rotation
     * to transform the z-axis to point onto 'position' and the y-axis to 'up'.
     * Unlike {@link Quaternion#lookAt} this method takes a world position to
     * look at not a relative direction.
     *
     * @param position
     *            where to look at in terms of world coordinates
     * @param upVector
     *            a vector indicating the (local) up direction. (typically {0,
     *            1, 0} in jME.)
     */
    public void lookAt(Vector3f position, Vector3f upVector) {
        assert TempVars.get().lock();
        Vector3f compVecA = TempVars.get().vect1;
        compVecA.set(position).subtractLocal(getWorldTranslation());
        getLocalRotation().lookAt(compVecA, upVector);
        assert TempVars.get().unlock();

        setTransformRefresh();
    }

    /**
     * Should be overriden by Node and Geometry.
     */
    protected void updateWorldBound(){
        // the world bound of a leaf is the same as it's model bound
        // for a node, the world bound is a combination of all it's children
        // bounds
        // -> handled by subclass
        refreshFlags &= ~RF_BOUND;
    }

    protected void updateWorldLightList(){
        if (parent == null){
            worldLights.update(localLights, null);
            refreshFlags &= ~RF_LIGHTLIST;
        }else{
            if ((parent.refreshFlags & RF_LIGHTLIST) == 0){
                worldLights.update(localLights, parent.worldLights);
                refreshFlags &= ~RF_LIGHTLIST;
            }else{
                assert false;
            }
        }
    }

    /**
     * Should only be called from updateGeometricState().
     * In most cases should not be subclassed.
     */
    protected void updateWorldTransforms(){
        if (parent == null){
            worldTransform.set(localTransform);
            refreshFlags &= ~RF_TRANSFORM;
        }else{
            if ((parent.refreshFlags & RF_TRANSFORM) == 0){
                // transform for parent is updated, can combine
                worldTransform.set(localTransform);
                worldTransform.combineWithParent(parent.worldTransform);
                refreshFlags &= ~RF_TRANSFORM;
            }else{
                // This part shouldn't really happen if the update
                // propegated from the top to bottom.
                assert false;
            }
        }
    }

    private void runControlUpdate(float tpf){
        if (controls.size() == 0)
            return;
        
        for (Entry<Control> entry : controls){
            entry.getValue().update(tpf);
        }
    }

    public void runControlRender(RenderManager rm, ViewPort vp){
        if (controls.size() == 0)
            return;

        for (Entry<Control> entry : controls){
            entry.getValue().render(rm, vp);
        }
    }

    public void setControl(Control control){
        controls.put(control.getType().ordinal(), control);
    }

    public void clearControl(ControlType type){
        controls.remove(type.ordinal());
    }

    public Control getControl(ControlType type){
        return controls.get(type.ordinal());
    }

    public int getNumControls(){
        return controls.size();
    }


    /**
     * <code>updateLogicalState</code> updates various logic state for
     * the node. This method should be overriden to provide specific 
     * functionality.
     * @param tpf Time per frame.
     */
    public void updateLogicalState(float tpf){
        runControlUpdate(tpf);
    }

    /**
     * <code>updateGeometricState</code> updates all the geometry information
     * for the node.
     *
     * @param time
     *            the frame time.
     * @param initiator
     *            true if this node started the update process.
     */
    public void updateGeometricState(){
        // assume that this Spatial is a leaf, a proper implementation
        // for this method should be provided by Node.

        // NOTE: Update world transforms first because
        // bound transform depends on them.
        if ((refreshFlags & RF_LIGHTLIST) != 0){
            updateWorldLightList();
        }
        if ((refreshFlags & RF_TRANSFORM) != 0){
            updateWorldTransforms();
        }
        if ((refreshFlags & RF_BOUND) != 0){
            updateWorldBound();
        }
    }

    /**
     * Convert a vector (in) from this spatials local coordinate space to world
     * coordinate space.
     *
     * @param in
     *            vector to read from
     * @param store
     *            where to write the result (null to create a new vector, may be
     *            same as in)
     * @return the result (store)
     */
    public Vector3f localToWorld(final Vector3f in, Vector3f store) {
        return worldTransform.transformVector(in, store);
    }

    /**
     * Convert a vector (in) from world coordinate space to this spatials local
     * coordinate space.
     *
     * @param in
     *            vector to read from
     * @param store
     *            where to write the result
     * @return the result (store)
     */
    public Vector3f worldToLocal(final Vector3f in, final Vector3f store) {
        return worldTransform.transformInverseVector(in, store);
    }

    /**
     * <code>getParent</code> retrieve's this node's parent. If the parent is
     * null this is the root node.
     *
     * @return the parent of this node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Called by {@link Node#attachChild(Spatial)} and
     * {@link Node#detachChild(Spatial)} - don't call directly.
     * <code>setParent</code> sets the parent of this node.
     *
     * @param parent
     *            the parent of this node.
     */
    protected void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * <code>removeFromParent</code> removes this Spatial from it's parent.
     *
     * @return true if it has a parent and performed the remove.
     */
    public boolean removeFromParent() {
        if (parent != null) {
            parent.detachChild(this);
            return true;
        }
        return false;
    }

    /**
     * determines if the provided Node is the parent, or parent's parent, etc. of this Spatial.
     *
     * @param ancestor
     *            the ancestor object to look for.
     * @return true if the ancestor is found, false otherwise.
     */
    public boolean hasAncestor(Node ancestor) {
        if (parent == null) {
            return false;
        } else if (parent.equals(ancestor)) {
            return true;
        } else {
            return parent.hasAncestor(ancestor);
        }
    }

    /**
     * <code>getLocalRotation</code> retrieves the local rotation of this
     * node.
     *
     * @return the local rotation of this node.
     */
    public Quaternion getLocalRotation() {
        return localTransform.getRotation();
    }

    /**
     * <code>setLocalRotation</code> sets the local rotation of this node.
     *
     * @param rotation
     *            the new local rotation.
     */
    public void setLocalRotation(Matrix3f rotation) {
        localTransform.getRotation().fromRotationMatrix(rotation);
        this.worldTransform.setRotation(this.localTransform.getRotation());
        setTransformRefresh();
    }

    /**
     * <code>setLocalRotation</code> sets the local rotation of this node,
     * using a quaterion to build the matrix.
     *
     * @param quaternion
     *            the quaternion that defines the matrix.
     */
    public void setLocalRotation(Quaternion quaternion) {
        localTransform.setRotation(quaternion);
        this.worldTransform.setRotation(this.localTransform.getRotation());
        setTransformRefresh();
    }

    /**
     * <code>getLocalScale</code> retrieves the local scale of this node.
     *
     * @return the local scale of this node.
     */
    public Vector3f getLocalScale() {
        return localTransform.getScale();
    }

    /**
     * <code>setLocalScale</code> sets the local scale of this node.
     *
     * @param localScale
     *            the new local scale, applied to x, y and z
     */
    public void setLocalScale(float localScale) {
        localTransform.setScale(localScale);
        worldTransform.setScale(localTransform.getScale());
        setTransformRefresh();
    }

    /**
     * <code>setLocalScale</code> sets the local scale of this node.
     *
     * @param localScale
     *            the new local scale
     */
    public void setLocalScale(float x, float y, float z) {
        localTransform.setScale(x, y, z);
        worldTransform.setScale(localTransform.getScale());
        setTransformRefresh();
    }

    /**
     * <code>setLocalScale</code> sets the local scale of this node.
     *
     * @param localScale
     *            the new local scale.
     */
    public void setLocalScale(Vector3f localScale) {
        localTransform.setScale(localScale);
        worldTransform.setScale(localTransform.getScale());
        setTransformRefresh();
    }

    /**
     * <code>getLocalTranslation</code> retrieves the local translation of
     * this node.
     *
     * @return the local translation of this node.
     */
    public Vector3f getLocalTranslation() {
        return localTransform.getTranslation();
    }

    /**
     * <code>setLocalTranslation</code> sets the local translation of this
     * node.
     *
     * @param localTranslation
     *            the local translation of this node.
     */
    public void setLocalTranslation(Vector3f localTranslation) {
        this.localTransform.setTranslation(localTranslation);
        this.worldTransform.setTranslation(this.localTransform.getTranslation());
        setTransformRefresh();
    }

    public void setLocalTranslation(float x, float y, float z) {
        this.localTransform.setTranslation(x,y,z);
        this.worldTransform.setTranslation(this.localTransform.getTranslation());
        setTransformRefresh();
    }

    public void setTransform(Transform t) {
        this.localTransform.set(t);
        setTransformRefresh();
    }

    public Transform getTransform(){
        return localTransform;
    }
    
    public void setMaterial(Material mat){
    }

    public void addLight(Light l){
        localLights.add(l);
        setLightListRefresh();
    }

    public Spatial move(float x, float y, float z){
        this.localTransform.getTranslation().addLocal(x, y, z);
        this.worldTransform.setTranslation(this.localTransform.getTranslation());
        setTransformRefresh();

        return this;
    }

    public Spatial move(Vector3f offset){
        this.localTransform.getTranslation().addLocal(offset);
        this.worldTransform.setTranslation(this.localTransform.getTranslation());
        setTransformRefresh();

        return this;
    }

    public Spatial scale(float x, float y, float z){
        this.localTransform.getScale().multLocal(x,y,z);
        this.worldTransform.setScale(this.localTransform.getScale());
        setTransformRefresh();

        return this;
    }

    public Spatial rotate(Quaternion rot){
        this.localTransform.getRotation().multLocal(rot);
        this.worldTransform.setRotation(this.localTransform.getRotation());
        setTransformRefresh();

        return this;
    }

    public Spatial rotate(float yaw, float roll, float pitch){
        assert TempVars.get().lock();
        Quaternion q = TempVars.get().quat1;
        q.fromAngles(yaw, roll, pitch);
        rotate(q);
        assert TempVars.get().unlock();

        return this;
    }

    public Spatial center(){
        if ((refreshFlags & RF_BOUND) != 0){
            updateGeometricState();
        }

        Vector3f worldTrans = getWorldTranslation();
        Vector3f worldCenter = getWorldBound().getCenter();

        Vector3f absTrans = worldTrans.subtract(worldCenter);
        setLocalTranslation(absTrans);

        return this;
    }

    /**
     * @see #setCullHint(CullHint)
     * @return the cull mode of this spatial, or if set to INHERIT, the cullmode
     *         of it's parent.
     */
    public CullHint getCullHint() {
        if (cullHint != CullHint.Inherit)
            return cullHint;
        else if (parent != null)
            return parent.getCullHint();
        else
            return CullHint.Dynamic;
    }


    /**
     * Returns this spatial's renderqueue bucket. If the mode is set to inherit,
     * then the spatial gets its renderqueue bucket from its parent.
     *
     * @return The spatial's current renderqueue mode.
     */
    public RenderQueue.Bucket getQueueBucket() {
        if (queueBucket != RenderQueue.Bucket.Inherit)
            return queueBucket;
        else if (parent != null)
            return parent.getQueueBucket();
        else
            return RenderQueue.Bucket.Opaque;
    }

    public RenderQueue.ShadowMode getShadowMode() {
        if (shadowMode != RenderQueue.ShadowMode.Inherit)
            return shadowMode;
        else if (parent != null)
            return parent.getShadowMode();
        else
            return ShadowMode.Off;
    }

    public void setLodLevel(int lod){
    }

    /**
     * <code>updateBound</code> recalculates the bounding object for this
     * Spatial.
     */
    public abstract void updateModelBound();

    /**
     * <code>setModelBound</code> sets the bounding object for this Spatial.
     *
     * @param modelBound
     *            the bounding object for this spatial.
     */
    public abstract void setModelBound(BoundingVolume modelBound);
//
//    /**
//     * checks this spatial against a second spatial, any collisions are stored
//     * in the results object.
//     *
//     * @param scene
//     *            the scene to test against.
//     * @param results
//     *            the results of the collisions.
//     */
//    public abstract void findCollisions(Spatial scene, CollisionResults results);
//
//
//    /**
//     * Tests a ray against this spatial, and stores the results in the result
//     * object.
//     *
//     * @param toTest
//     *            ray to test picking against
//     * @param results
//     *            the results of the picking
//     */
//    public abstract void findPick(Ray toTest, PickResults results);

    public abstract int getVertexCount();

    public abstract int getTriangleCount();

    @Override
    public Spatial clone(){
        try{
            Spatial clone = (Spatial) super.clone();
            if (worldBound != null)
                clone.worldBound = worldBound.clone();
            clone.worldLights = worldLights.clone();
            clone.localLights = localLights.clone();
            clone.worldTransform = worldTransform.clone();
            clone.localTransform = localTransform.clone();

            if (clone instanceof Node){
                Node node = (Node) this;
                Node nodeClone = (Node) clone;
                nodeClone.children = new ArrayList<Spatial>();
                for (Spatial child : node.children){
                    Spatial childClone = child.clone();
                    childClone.parent = nodeClone;
                    nodeClone.children.add(childClone);
                }
            }

            clone.parent = null;
            clone.setBoundRefresh();
            clone.setTransformRefresh();
            clone.setLightListRefresh();

            clone.controls = new IntMap<Control>();
            for (Entry<Control> c : controls){
                clone.controls.put( c.getKey(), c.getValue().cloneForSpatial(clone) );
            }
            return clone;
        }catch (CloneNotSupportedException ex){
            throw new AssertionError();
        }
    }

    /**
     * Create a deep clone, including of the mesh (copy the buffers).
     * @return
     */
    public abstract Spatial deepClone();

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(name, "name", null);
        capsule.write(worldBound, "world_bound", null);
        capsule.write(cullHint, "cull_mode", CullHint.Inherit);
        capsule.write(queueBucket, "queue", RenderQueue.Bucket.Inherit);
        capsule.write(shadowMode, "shadow_mode", ShadowMode.Inherit);
        capsule.write(localTransform, "transform", new Transform());
        capsule.write(localLights, "lights", null);
        capsule.writeIntSavableMap(controls, "controls", null);

//        capsule.writeStringSavableMap(UserDataManager.getInstance().getAllData(
//                this), "userData", null);
    }

//    @SuppressWarnings("unchecked")
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", null);
        worldBound = (BoundingVolume) ic.readSavable("world_bound", null);
        cullHint = ic.readEnum("cull_mode", CullHint.class, CullHint.Inherit);
        queueBucket = ic.readEnum("queue", RenderQueue.Bucket.class,
                                    RenderQueue.Bucket.Inherit);
        shadowMode = ic.readEnum("shadow_mode", ShadowMode.class,
                                    ShadowMode.Inherit);

        localTransform = (Transform) ic.readSavable("transform", Transform.Identity);
        localLights = (LightList) ic.readSavable("lights", null);
        controls = (IntMap<Control>) ic.readIntSavableMap("controls", null);
        // world lights and world transform already initialized

//        HashMap<String, Savable> map = (HashMap<String, Savable>) capsule
//                .readStringSavableMap("userData", null);
//        if (map != null) {
//            UserDataManager.getInstance().setAllData(this, map);
//        }

        
    }

    /**
     * <code>getWorldBound</code> retrieves the world bound at this node
     * level.
     *
     * @return the world bound at this level.
     */
    public BoundingVolume getWorldBound() {
        return worldBound;
    }

    /**
     * <code>setCullHint</code> sets how scene culling should work on this
     * spatial during drawing. CullHint.Dynamic: Determine via the defined
     * Camera planes whether or not this Spatial should be culled.
     * CullHint.Always: Always throw away this object and any children during
     * draw commands. CullHint.Never: Never throw away this object (always draw
     * it) CullHint.Inherit: Look for a non-inherit parent and use its cull
     * mode. NOTE: You must set this AFTER attaching to a parent or it will be
     * reset with the parent's cullMode value.
     *
     * @param hint
     *            one of CullHint.Dynamic, CullHint.Always, CullHint.Inherit or
     *            CullHint.Never
     */
    public void setCullHint(CullHint hint) {
        cullHint = hint;
    }

    /**
     * @return the cullmode set on this Spatial
     */
    public CullHint getLocalCullHint() {
        return cullHint;
    }

    /**
     * <code>setQueueBucket</code> determines at what phase of the
     * rendering proces this Spatial will rendered. There are 4 different
     * phases: Bucket.Opaque - The renderer will
     * try to find the optimal order for rendering all objects using this mode.
     * You should use this mode for most normal objects, except transparant
     * ones, as it could give a nice performance boost to your application.
     * Bucket.Transparent - This is the mode you should use for object with
     * transparancy in them. It will ensure the objects furthest away are
     * rendered first. That ensures when another transparent object is drawn on
     * top of previously drawn objects, you can see those (and the object drawn
     * using Opaque) through the tranparant parts of the newly drawn
     * object. Bucket.Gui - This is a special mode, for drawing 2D object
     * without prespective (such as GUI or HUD parts) Lastly, there is a special
     * mode, Bucket.Inherit, that will ensure that this spatial uses the same
     * mode as the parent Node does.
     *
     * @param queueBucket
     *            The bucket to use for this Spatial.
     */
    public void setQueueBucket(RenderQueue.Bucket queueBucket) {
        this.queueBucket = queueBucket;
    }

    public void setShadowMode(RenderQueue.ShadowMode shadowMode){
        this.shadowMode = shadowMode;
    }

    /**
     * @return
     */
    public RenderQueue.Bucket getLocalQueueBucket() {
        return queueBucket;
    }

    public RenderQueue.ShadowMode getLocalShadowMode() {
        return shadowMode;
    }

    /**
     * Returns this spatial's last frustum intersection result. This int is set
     * when a check is made to determine if the bounds of the object fall inside
     * a camera's frustum. If a parent is found to fall outside the frustum, the
     * value for this spatial will not be updated.
     *
     * @return The spatial's last frustum intersection result.
     */
    public Camera.FrustumIntersect getLastFrustumIntersection() {
        return frustrumIntersects;
    }

    /**
     * Overrides the last intersection result. This is useful for operations
     * that want to start rendering at the middle of a scene tree and don't want
     * the parent of that node to influence culling. (See texture renderer code
     * for example.)
     *
     * @param intersects
     *            the new value
     */
    public void setLastFrustumIntersection(Camera.FrustumIntersect intersects) {
        frustrumIntersects = intersects;
    }

    /**
     * Returns the Spatial's name followed by the class of the spatial <br>
     * Example: "MyNode (com.jme.scene.Spatial)
     *
     * @return Spatial's name followed by the class of the Spatial
     */
    @Override
    public String toString() {
        return name + " (" + this.getClass().getSimpleName() + ')';
    }

    public Matrix4f getLocalToWorldMatrix(Matrix4f store) {
        if (store == null) {
            store = new Matrix4f();
        } else {
            store.loadIdentity();
        }
        // multiply with scale first, then rotate, finally translate (cf.
        // Eberly)
        store.scale(getWorldScale());
        store.multLocal(getWorldRotation());
        store.setTranslation(getWorldTranslation());
        return store;
    }
    
}

