/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.scene;

import com.g3d.bounding.BoundingVolume;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.light.Light;
import com.g3d.light.LightList;
import com.g3d.material.Material;
import com.g3d.math.Matrix3f;
import com.g3d.math.Matrix4f;
import com.g3d.math.Quaternion;
import com.g3d.math.Transform;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.util.TempVars;
import java.io.IOException;


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
public abstract class Spatial implements Savable {

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

    public transient float queueDistance = Float.NEGATIVE_INFINITY;

    protected Material material;

    protected Transform localTransform;

    protected Transform worldTransform;

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
            frustrumIntersects = cam.contains(worldBound);
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
        Vector3f compVecA = TempVars.get().vect1;
        Quaternion q = TempVars.get().quat1;

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
        Vector3f compVecA = TempVars.get().vect1;
        compVecA.set(position).subtractLocal(getWorldTranslation());
        getLocalRotation().lookAt(compVecA, upVector);

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

    /**
     * <code>updateGeometricState</code> updates all the geometry information
     * for the node.
     *
     * @param time
     *            the frame time.
     * @param initiator
     *            true if this node started the update process.
     */
    public void updateGeometricState(float time, boolean initiator){
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

//    /**
//     * Convert a vector (in) from this spatials local coordinate space to world
//     * coordinate space.
//     *
//     * @param in
//     *            vector to read from
//     * @param store
//     *            where to write the result (null to create a new vector, may be
//     *            same as in)
//     * @return the result (store)
//     */
//    public Vector3f localToWorld(final Vector3f in, Vector3f store) {
//        if (store == null)
//            store = new Vector3f();
//        // multiply with scale first, then rotate, finally translate (cf.
//        // Eberly)
//        return getWorldRotation().mult(
//                store.set(in).multLocal(getWorldScale()), store).addLocal(
//                getWorldTranslation());
//    }
//
//    /**
//     * Convert a vector (in) from world coordinate space to this spatials local
//     * coordinate space.
//     *
//     * @param in
//     *            vector to read from
//     * @param store
//     *            where to write the result
//     * @return the result (store)
//     */
//    public Vector3f worldToLocal(final Vector3f in, final Vector3f store) {
//        in.subtract(getWorldTranslation(), store).divideLocal(getWorldScale());
//        getWorldRotation().inverse().mult(store, store);
//        return store;
//    }
//
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
        this.worldTransform.setRotationQuaternion(this.localTransform.getRotation());
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
        localTransform.setRotationQuaternion(quaternion);
        this.worldTransform.setRotationQuaternion(this.localTransform.getRotation());
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

    public void setMaterial(Material material){
        this.material = material;
    }

    public Material getMaterial(){
        return material;
    }

    public void addLight(Light l){
        localLights.add(l);
        setLightListRefresh();
    }

    //
//    /**
//     * Sets the zOrder of this Spatial and, if setOnChildren is true, all
//     * children as well. This value is used in conjunction with the RenderQueue
//     * and QUEUE_ORTHO for determining draw order.
//     *
//     * @param zOrder
//     *            the new zOrder.
//     * @param setOnChildren
//     *            if true, children will also have their zOrder set to the given
//     *            value.
//     */
//    public void setZOrder(int zOrder, boolean setOnChildren) {
//        setZOrder(zOrder);
//        if (setOnChildren) {
//            if (this instanceof Node) {
//                Node n = (Node) this;
//                if (n.getChildren() != null) {
//                    for (Spatial child : n.getChildren()) {
//                        child.setZOrder(zOrder, true);
//                    }
//                }
//            }
//        }
//    }
//
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


//    /**
//     * Returns this spatial's renderqueue mode. If the mode is set to inherit,
//     * then the spatial gets its renderqueue mode from its parent.
//     *
//     * @return The spatial's current renderqueue mode.
//     */
//    public int getRenderQueueMode() {
//        if (renderQueueMode != Renderer.QUEUE_INHERIT)
//            return renderQueueMode;
//        else if (parent != null)
//            return parent.getRenderQueueMode();
//        else
//            return Renderer.QUEUE_SKIP;
//    }

//
//    /**
//     * Returns this spatial's normals mode. If the mode is set to inherit, then
//     * the spatial gets its normals mode from its parent.
//     *
//     * @return The spatial's current normals mode.
//     */
//    public NormalsMode getNormalsMode() {
//        if (normalsMode != NormalsMode.Inherit)
//            return normalsMode;
//        else if (parent != null)
//            return parent.getNormalsMode();
//        else
//            return NormalsMode.NormalizeIfScaled;
//    }
//
//    /**
//     * Called during updateRenderState(Stack[]), this function goes up the scene
//     * graph tree until the parent is null and pushes RenderStates onto the
//     * states Stack array.
//     *
//     * @param states
//     *            The Stack[] to push states onto.
//     */
//    @SuppressWarnings("unchecked")
//    public void propagateStatesFromRoot(Stack[] states) {
//        // traverse to root to allow downward state propagation
//        if (parent != null)
//            parent.propagateStatesFromRoot(states);
//
//        // push states onto current render state stack
//        for (int x = 0; x < RenderState.RS_MAX_STATE; x++)
//            if (getRenderState(x) != null)
//                states[x].push(getRenderState(x));
//    }

    /**
     * <code>propagateBoundToRoot</code> passes the new world bound up the
     * tree to the root.
     */
//    public void propagateBoundToRoot() {
//        if (parent != null) {
//            parent.updateWorldBound();
//            parent.propagateBoundToRoot();
//        }
//    }
//
//    /**
//     * <code>calculateCollisions</code> calls findCollisions to populate the
//     * CollisionResults object then processes the collision results.
//     *
//     * @param scene
//     *            the scene to test against.
//     * @param results
//     *            the results object.
//     */
//    public void calculateCollisions(Spatial scene, CollisionResults results) {
//        findCollisions(scene, results);
//        results.processCollisions();
//    }

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
//    /**
//     * Checks this spatial against a second spatial for collisions.
//     *
//     * @param scene
//     *            the scene to test against.
//     * @param checkTriangles
//     *            check for collisions on triangle accuracy level
//     * @return true if any collision were found
//     */
//    public abstract boolean hasCollision(Spatial scene, boolean checkTriangles);
//
//    public void calculatePick(Ray ray, PickResults results) {
//        findPick(ray, results);
//        results.processPick();
//    }
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
//
//    /**
//     * Stores user define data for this Spatial.
//     *
//     * @param key
//     *            the key component to retrieve the data from the hash map.
//     * @param data
//     *            the data to store.
//     */
//    public void setUserData(String key, Savable data) {
//        UserDataManager.getInstance().setUserData(this, key, data);
//    }
//
//    /**
//     * Retrieves user data from the hashmap defined by the provided key.
//     *
//     * @param key
//     *            the key of the data to obtain.
//     * @return the data referenced by the key. If the key is invalid, null is
//     *         returned.
//     */
//    public Savable getUserData(String key) {
//        return UserDataManager.getInstance().getUserData(this, key);
//    }
//
//    /**
//     * Removes user data from the hashmap defined by the provided key.
//     *
//     * @param key
//     *            the key of the data to remove.
//     * @return the data that has been removed, null if no data existed.
//     */
//    public Savable removeUserData(String key) {
//        return UserDataManager.getInstance().removeUserData(this, key);
//    }
//
//    public abstract int getVertexCount();
//
//    public abstract int getTriangleCount();

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(name, "name", null);
        capsule.write(worldBound, "worldBound", null);
//        capsule.write(material, "material", null);
//        capsule.write(localLights, "localLights", null);

//        capsule.write(isCollidable, "isCollidable", true);
        capsule.write(cullHint, "cullMode", CullHint.Inherit);



//        capsule.write(renderQueueMode, "renderQueueMode",
//                Renderer.QUEUE_INHERIT);
//        capsule.write(zOrder, "zOrder", 0);

        capsule.write(localTransform, "localTransform", new Transform());

//        capsule.writeStringSavableMap(UserDataManager.getInstance().getAllData(
//                this), "userData", null);
//
//        capsule.writeSavableArrayList(geometricalControllers,
//                "geometricalControllers", null);
    }

//    @SuppressWarnings("unchecked")
    public void read(G3DImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        name = capsule.readString("name", null);
//        isCollidable = capsule.readBoolean("isCollidable", true);
        cullHint = capsule.readEnum("cullMode", CullHint.class,
                CullHint.Inherit);

//        renderQueueMode = capsule.readInt("renderQueueMode",
//                Renderer.QUEUE_INHERIT);
//        zOrder = capsule.readInt("zOrder", 0);
//        lightCombineMode = capsule.readEnum("lightCombineMode", LightCombineMode.class,
//                LightCombineMode.Inherit);
//        textureCombineMode = capsule.readEnum("textureCombineMode", TextureCombineMode.class,
//                TextureCombineMode.Inherit);
//        normalsMode = capsule.readEnum("normalsMode", NormalsMode.class,
//                NormalsMode.Inherit);

//        Savable[] savs = capsule.readSavableArray("renderStateList", null);
//        if (savs == null)
//            renderStateList = null;
//        else {
//            renderStateList = new RenderState[savs.length];
//            for (int x = 0; x < savs.length; x++) {
//                renderStateList[x] = (RenderState) savs[x];
//            }
//        }

        localTransform = (Transform) capsule.readSavable("localTransform", new Transform());

//        HashMap<String, Savable> map = (HashMap<String, Savable>) capsule
//                .readStringSavableMap("userData", null);
//        if (map != null) {
//            UserDataManager.getInstance().setAllData(this, map);
//        }
//
//        geometricalControllers = capsule.readSavableArrayList(
//                "geometricalControllers", null);

        worldTransform = new Transform();
    }
//
//    /**
//     * Sets if this Spatial is to be used in intersection (collision and
//     * picking) calculations. By default this is true.
//     *
//     * @param isCollidable
//     *            true if this Spatial is to be used in intersection
//     *            calculations, false otherwise.
//     */
//    public void setIsCollidable(boolean isCollidable) {
//        this.isCollidable = isCollidable;
//    }
//
//    /**
//     * Defines if this Spatial is to be used in intersection (collision and
//     * picking) calculations. By default this is true.
//     *
//     * @return true if this Spatial is to be used in intersection calculations,
//     *         false otherwise.
//     */
//    public boolean isCollidable() {
//        return this.isCollidable;
//    }

    /**
     * <code>getWorldBound</code> retrieves the world bound at this node
     * level.
     *
     * @return the world bound at this level.
     */
    public BoundingVolume getWorldBound() {
        return worldBound;
    }

//    /**
//     * <code>draw</code> abstract method that handles drawing data to the
//     * renderer if it is geometry and passing the call to it's children if it is
//     * a node.
//     *
//     * @param r
//     *            the renderer used for display.
//     */
//    public abstract void draw(Renderer r);

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
     * <code>updateWorldBound</code> updates the bounding volume of the world.
     * Abstract, geometry transforms the bound while node merges the children's
     * bound. In most cases, users will want to call updateModelBound() and let
     * this function be called automatically during updateGeometricState().
     */
//    public abstract void updateWorldBound();

//
//    public void sortLights() {
//    }
//
//
//    /**
//     * <code>setRenderQueueMode</code> determines at what phase of the
//     * rendering proces this Spatial will rendered. There are 4 different
//     * phases: QUEUE_SKIP - The spatial will be drawn as soon as possible,
//     * before the other phases of rendering. QUEUE_OPAQUE - The renderer will
//     * try to find the optimal order for rendering all objects using this mode.
//     * You should use this mode for most normal objects, except transparant
//     * ones, as it could give a nice performance boost to your application.
//     * QUEUE_TRANSPARENT - This is the mode you should use for object with
//     * transparancy in them. It will ensure the objects furthest away are
//     * rendered first. That ensures when another transparent object is drawn on
//     * top of previously drawn objects, you can see those (and the object drawn
//     * using SKIP and OPAQUE) through the tranparant parts of the newly drawn
//     * object. QUEUE_ORTHO - This is a special mode, for drawing 2D object
//     * without prespective (such as GUI or HUD parts) Lastly, there is a special
//     * mode, QUEUE_INHERIT, that will ensure that this spatial uses the same
//     * mode as the parent Node does.
//     *
//     * @param renderQueueMode
//     *            The mode to use for this Spatial.
//     */
//    public void setRenderQueueMode(int renderQueueMode) {
//        this.renderQueueMode = renderQueueMode;
//    }
//
//    /**
//     * @return
//     */
//    public int getLocalRenderQueueMode() {
//        return renderQueueMode;
//    }
//
//    /**
//     * @param zOrder
//     */
//    public void setZOrder(int zOrder) {
//        this.zOrder = zOrder;
//    }
//
//    /**
//     * @return
//     */
//    public int getZOrder() {
//        return zOrder;
//    }
//
//    /**
//     * @return
//     */
//    public NormalsMode getLocalNormalsMode() {
//        return normalsMode;
//    }
//
//    /**
//     * @param mode
//     */
//    public void setNormalsMode(NormalsMode mode) {
//        this.normalsMode = mode;
//    }

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

//    public Class<? extends Spatial> getClassTag() {
//        return this.getClass();
//    }
}

