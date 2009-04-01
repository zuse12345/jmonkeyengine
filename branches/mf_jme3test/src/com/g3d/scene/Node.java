/*
 * Copyright (c) 2003-2008 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.g3d.scene;

import com.g3d.bounding.BoundingVolume;
import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.Savable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <code>Node</code> defines an internal node of a scene graph. The internal
 * node maintains a collection of children and handles merging said children
 * into a single bound to allow for very fast culling of multiple nodes. Node
 * allows for any number of children to be attached.
 * 
 * @author Mark Powell
 * @author Gregg Patton
 * @author Joshua Slack
 */
public class Node extends Spatial implements Savable {

    private static final Logger logger = Logger.getLogger(Node.class.getName());


    /** 
     * This node's children.
     */
    protected List<Spatial> children = Collections.synchronizedList(new ArrayList<Spatial>(1));;

    /**
     * Default constructor.
     */
    public Node() {
    }

    /**
     * Constructor instantiates a new <code>Node</code> with a default empty
     * list for containing children.
     * 
     * @param name
     *            the name of the scene element. This is required for
     *            identification and comparision purposes.
     */
    public Node(String name) {
        super(name);
    }

    /**
     * 
     * <code>getQuantity</code> returns the number of children this node
     * maintains.
     * 
     * @return the number of children this node maintains.
     */
    public int getQuantity() {
        if(children == null) {
            return 0;
        } 
            
        return children.size();        
    }

    @Override
    protected void setTransformRefresh(){
        super.setTransformRefresh();
        for (Spatial child : children){
            if ((child.refreshFlags & RF_TRANSFORM) != 0)
                return;

            child.setTransformRefresh();
        }
    }

    @Override
    protected void setLightListRefresh(){
        super.setLightListRefresh();
        for (Spatial child : children){
            if ((child.refreshFlags & RF_LIGHTLIST) != 0)
                return;

            child.setLightListRefresh();
        }
    }

    @Override
    protected void updateWorldBound(){
        super.updateWorldBound();
        // for a node, the world bound is a combination of all it's children
        // bounds
        BoundingVolume resultBound = null;
        for (int i = 0, cSize = children.size(); i < cSize; i++) {
            Spatial child = children.get(i);
            // child bound is assumed to be updated
            assert (child.refreshFlags & RF_BOUND) == 0;
            if (resultBound != null) {
                // merge current world bound with child world bound
                resultBound.mergeLocal(child.getWorldBound());
            } else {
                // set world bound to first non-null child world bound
                if (child.getWorldBound() != null) {
                    resultBound = child.getWorldBound().clone(this.worldBound);
                }
            }
        }
        this.worldBound = resultBound;
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
    @Override
    public void updateGeometricState(float tpf, boolean initiator){
        if ((refreshFlags & RF_LIGHTLIST) != 0){
            updateWorldLightList();
        }

        if ((refreshFlags & RF_TRANSFORM) != 0){
            // combine with parent transforms- same for all spatial
            // subclasses.
            updateWorldTransforms();
        }

        // the important part- make sure child geometric state is refreshed
        // first before updating own world bound. This saves
        // a round-trip later on.
        for (int i = 0, cSize = children.size(); i < cSize; i++) {
            Spatial child = children.get(i);
            child.updateGeometricState(tpf, false);
        }

        // XXX: Room for optimization, merge the above loop with this one.
        if ((refreshFlags & RF_BOUND) != 0){
            updateWorldBound();
        }
    }

    /**
     * <code>getTriangleCount</code> returns the number of triangles contained
     * in all sub-branches of this node that contain geometry.
     * @return the triangle count of this branch.
     */
//    @Override
//    public int getTriangleCount() {
//        int count = 0;
//        if(children != null) {
//            for(int i = 0; i < children.size(); i++) {
//                count += children.get(i).getTriangleCount();
//            }
//        }
//
//        return count;
//    }
    
    /**
     * <code>getVertexCount</code> returns the number of vertices contained
     * in all sub-branches of this node that contain geometry.
     * @return the vertex count of this branch.
     */
//    @Override
//    public int getVertexCount() {
//        int count = 0;
//        if(children != null) {
//            for(int i = 0; i < children.size(); i++) {
//               count += children.get(i).getVertexCount();
//            }
//        }
//
//        return count;
//    }

    /**
     * 
     * <code>attachChild</code> attaches a child to this node. This node
     * becomes the child's parent. The current number of children maintained is
     * returned.
     * <br>
     * If the child already had a parent it is detached from that former parent.
     * 
     * @param child
     *            the child to attach to this node.
     * @return the number of children maintained by this node.
     */
    public int attachChild(Spatial child) {
        if (child != null) {
            if (child.getParent() != this) {
                if (child.getParent() != null) {
                    child.getParent().detachChild(child);
                }
                child.setParent(this);
                children.add(child);
                
                // XXX: Not entirely correct? Forces bound update up the 
                // tree stemming from the attached child. Also forces
                // transform update down the tree-
                child.setTransformRefresh();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Child (" + child.getName()
                            + ") attached to this" + " node (" + getName()
                            + ")");
                }
            }
        }
        
        return children.size();
    }
    
    /**
     * 
     * <code>attachChildAt</code> attaches a child to this node at an index. This node
     * becomes the child's parent. The current number of children maintained is
     * returned.
     * <br>
     * If the child already had a parent it is detached from that former parent.
     * 
     * @param child
     *            the child to attach to this node.
     * @return the number of children maintained by this node.
     */
    public int attachChildAt(Spatial child, int index) {
        if (child != null) {
            if (child.getParent() != this) {
                if (child.getParent() != null) {
                    child.getParent().detachChild(child);
                }
                child.setParent(this);
                children.add(index, child);
                child.setTransformRefresh();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Child (" + child.getName()
                            + ") attached to this" + " node (" + getName()
                            + ")");
                }
            }
        }

        return children.size();
    }

    /**
     * <code>detachChild</code> removes a given child from the node's list.
     * This child will no longe be maintained.
     * 
     * @param child
     *            the child to remove.
     * @return the index the child was at. -1 if the child was not in the list.
     */
    public int detachChild(Spatial child) {
        if (child == null)
            return -1;

        if (child.getParent() == this) {
            int index = children.indexOf(child);
            if (index != -1) {
                detachChildAt(index);
            }
            return index;
        } 
            
        return -1;        
    }

    /**
     * <code>detachChild</code> removes a given child from the node's list.
     * This child will no longe be maintained. Only the first child with a
     * matching name is removed.
     * 
     * @param childName
     *            the child to remove.
     * @return the index the child was at. -1 if the child was not in the list.
     */
    public int detachChildNamed(String childName) {
        if (childName == null)
            return -1;
        for (int x = 0, max = children.size(); x < max; x++) {
            Spatial child =  children.get(x);
            if (childName.equals(child.getName())) {
                detachChildAt( x );
                return x;
            }
        }
        return -1;
    }

    /**
     * 
     * <code>detachChildAt</code> removes a child at a given index. That child
     * is returned for saving purposes.
     * 
     * @param index
     *            the index of the child to be removed.
     * @return the child at the supplied index.
     */
    public Spatial detachChildAt(int index) {
        Spatial child =  children.remove(index);
        if ( child != null ) {
            child.setParent( null );
            logger.info("Child removed.");

            // since a child with a bound was detached;
            // our own bound will probably change.
            setBoundRefresh();

            // our world transform no longer influences the child.
            // XXX: Not neccessary? Since child will have transform updated
            // when attached anyway.
            child.setTransformRefresh();
        }
        return child;
    }

    /**
     * 
     * <code>detachAllChildren</code> removes all children attached to this
     * node.
     */
    public void detachAllChildren() {
        for ( int i = children.size() - 1; i >= 0; i-- ) {
            detachChildAt(i);
        }
        logger.info("All children removed.");
    }

    public int getChildIndex(Spatial sp) {
        return children.indexOf(sp);
    }

    /**
     * More efficent than e.g detaching and attaching as no updates are needed.
     * @param index1
     * @param index2
     */
    public void swapChildren(int index1, int index2) {
        Spatial c2 =  children.get(index2);
        Spatial c1 =  children.remove(index1);
        children.add(index1, c2);
        children.remove(index2);
        children.add(index2, c1);
    }

    /**
     * 
     * <code>getChild</code> returns a child at a given index.
     * 
     * @param i
     *            the index to retrieve the child from.
     * @return the child at a specified index.
     */
    public Spatial getChild(int i) {
        return children.get(i);
    }

    /**
     * <code>getChild</code> returns the first child found with exactly the
     * given name (case sensitive.)
     * 
     * @param name
     *            the name of the child to retrieve. If null, we'll return null.
     * @return the child if found, or null.
     */
    public Spatial getChild(String name) {
        if (name == null) return null;
        for (int x = 0, cSize = getQuantity(); x < cSize; x++) {
            Spatial child = children.get(x);
            if (name.equals(child.getName())) {
                return child;
            } else if(child instanceof Node) {
                Spatial out = ((Node)child).getChild(name);
                if(out != null) {
                    return out;
                }
            }
        }
        return null;
    }
    
    /**
     * determines if the provided Spatial is contained in the children list of
     * this node.
     * 
     * @param spat
     *            the child object to look for.
     * @return true if the object is contained, false otherwise.
     */
    public boolean hasChild(Spatial spat) {
        if (children.contains(spat))
            return true;

        for (int i = 0, max = getQuantity(); i < max; i++) {
            Spatial child =  children.get(i);
            if (child instanceof Node && ((Node) child).hasChild(spat))
                return true;
        }

        return false;
    }

//    /**
//     * <code>updateWorldData</code> updates all the children maintained by
//     * this node.
//     *
//     * @param time
//     *            the frame time.
//     */
//    @Override
//    public void updateWorldData(float time) {
//        super.updateWorldData(time);
//
//        Spatial child;
//        for (int i = 0, n = getQuantity(); i < n; i++) {
//            try {
//                child = children.get(i);
//            } catch (IndexOutOfBoundsException e) {
//                // a child was removed in updateGeometricState (note: this may
//                // skip one child)
//                break;
//            }
//            if (child != null) {
//                child.updateGeometricState(time, false);
//            }
//        }
//    }

//    @Override
//    public void updateWorldVectors(boolean recurse) {
//        if (((lockedMode & Spatial.LOCKED_TRANSFORMS) == 0)) {
//            updateWorldScale();
//            updateWorldRotation();
//            updateWorldTranslation();
//
//            if (recurse) {
//                for (int i = 0, n = getQuantity(); i < n; i++) {
//                    children.get(i).updateWorldVectors(true);
//                }
//            }
//        }
//    }
//    /**
//     * <code>draw</code> calls the onDraw method for each child maintained by
//     * this node.
//     *
//     * @see com.jme.scene.Spatial#draw(com.jme.renderer.Renderer)
//     * @param r
//     *            the renderer to draw to.
//     */
//    @Override
//    public void draw(Renderer r) {
//        if(children == null) {
//            return;
//        }
//        Spatial child;
//        for (int i = 0, cSize = children.size(); i < cSize; i++) {
//            child =  children.get(i);
//            if (child != null)
//                child.onDraw(r);
//        }
//    }
//
//    /**
//     * Applies the stack of render states to each child by calling
//     * updateRenderState(states) on each child.
//     *
//     * @param states
//     *            The Stack[] of render states to apply to each child.
//     */
//    @Override
//    protected void applyRenderState(Stack<? extends RenderState>[] states) {
//        if(children == null) {
//            return;
//        }
//        for (int i = 0, cSize = children.size(); i < cSize; i++) {
//            Spatial pkChild = getChild(i);
//            if (pkChild != null)
//                pkChild.updateRenderState(states);
//        }
//    }
//
//    @Override
//    public void sortLights() {
//        if(children == null) {
//            return;
//        }
//        for (int i = 0, cSize = children.size(); i < cSize; i++) {
//            Spatial pkChild = getChild(i);
//            if (pkChild != null)
//                pkChild.sortLights();
//        }
//    }

    /**
     * <code>updateWorldBound</code> merges the bounds of all the children
     * maintained by this node. This will allow for faster culling operations.
     * 
     * @see com.jme.scene.Spatial#updateWorldBound()
     */
//    @Override
//    public void updateWorldBound() {
//        // TODO: bound locking?
//        //if ((lockedMode & Spatial.LOCKED_BOUNDS) != 0) return;
//        BoundingVolume worldBound = null;
//        for (int i = 0, cSize = children.size(); i < cSize; i++) {
//            Spatial child =  children.get(i);
//            if (child != null) {
//                if (worldBound != null) {
//                    // merge current world bound with child world bound
//                    worldBound.mergeLocal(child.getWorldBound());
//
//                } else {
//                    // set world bound to first non-null child world bound
//                    if (child.getWorldBound() != null) {
//                        worldBound = child.getWorldBound().clone(this.worldBound);
//                    }
//                }
//            }
//        }
//        this.worldBound = worldBound;
//    }
//
//    @Override
//    public void findCollisions(Spatial scene, CollisionResults results) {
//        if (getWorldBound() != null && isCollidable && scene.isCollidable()) {
//            if (getWorldBound().intersects(scene.getWorldBound())) {
//                // further checking needed.
//                for (int i = 0; i < getQuantity(); i++) {
//                    getChild(i).findCollisions(scene, results);
//                }
//            }
//        }
//    }
//
//    @Override
//    public boolean hasCollision(Spatial scene, boolean checkTriangles) {
//        if (getWorldBound() != null && isCollidable && scene.isCollidable()) {
//            if (getWorldBound().intersects(scene.getWorldBound())) {
//                if(children == null && !checkTriangles) {
//                    return true;
//                }
//                // further checking needed.
//                for (int i = 0; i < getQuantity(); i++) {
//                    if (getChild(i).hasCollision(scene, checkTriangles)) {
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public void findPick(Ray toTest, PickResults results) {
//        if(children == null) {
//            return;
//        }
//        if (getWorldBound() != null && isCollidable) {
//            if (getWorldBound().intersects(toTest)) {
//                // further checking needed.
//                for (int i = 0; i < getQuantity(); i++) {
//                    ( children.get(i)).findPick(toTest, results);
//                }
//            }
//        }
//    }

	/**
	 * Returns all children to this node.
	 *
	 * @return a list containing all children to this node
	 */
	public List<Spatial> getChildren() {
        return children;
    }

    public void childChange(Geometry geometry, int index1, int index2) {
        //just pass to parent
        if(parent != null) {
            parent.childChange(geometry, index1, index2);
        }
    }

    public void write(G3DExporter e) throws IOException {
//        super.write(e);
//        if (children == null)
//            e.getCapsule(this).writeSavableArrayList(null, "children", null);
//        else
//            e.getCapsule(this).writeSavableArrayList(new ArrayList<Spatial>(children), "children", null);
    }
//
    @SuppressWarnings("unchecked")
    public void read(G3DImporter e) throws IOException {
//        super.read(e);
//        ArrayList<Spatial> cList = e.getCapsule(this).readSavableArrayList("children", null);
//        if (cList == null)
//            children = null;
//        else
//            children = Collections.synchronizedList(cList);
//
//        // go through children and set parent to this node
//        if (children != null) {
//            for (int x = 0, cSize = children.size(); x < cSize; x++) {
//                Spatial child = children.get(x);
//                child.parent = this;
//            }
//        }
    }

    @Override
    public void setModelBound(BoundingVolume modelBound) {
        if(children != null) {
            for(int i = 0, max = children.size(); i < max; i++) {
                children.get(i).setModelBound(modelBound != null ? modelBound.clone(null) : null);
            }
        }
    }

    @Override
    public void updateModelBound() {
        if(children != null) {
            for(int i = 0, max = children.size(); i < max; i++) {
                children.get(i).updateModelBound();
            }
        }
    }

}
