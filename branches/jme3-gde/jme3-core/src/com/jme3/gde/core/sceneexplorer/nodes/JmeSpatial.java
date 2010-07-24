/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.sceneexplorer.nodes;

import com.jme3.bounding.BoundingVolume;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.sceneexplorer.nodes.properties.JmeProperty;
import com.jme3.light.LightList;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author normenhansen
 */
@org.openide.util.lookup.ServiceProvider(service=SceneExplorerNode.class)
public class JmeSpatial extends AbstractNode implements SceneExplorerNode{

    private Spatial spatial;
    private JmeChildren factory;
    private final InstanceContent lookupContents;
    private Lookup lookup;
    private SaveCookie saveCookie = new SaveCookieImpl();
    protected final DataFlavor SPATIAL_FLAVOR = new DataFlavor(ClipboardSpatial.class, "Spatial");

    public JmeSpatial() {
        super(Children.LEAF);
        lookupContents = null;
    }

    public JmeSpatial(Spatial spatial, JmeChildren factory) {
        super(factory, new JmeLookup(new InstanceContent()));
        this.factory = factory;
        this.spatial = spatial;
        lookupContents = ((JmeLookup) getLookup()).getInstanceContent();
        getLookupContents().add(spatial);
        getLookupContents().add(this);
        setName(spatial.getName());
    }

    public InstanceContent getLookupContents() {
        return lookupContents;
    }

    public JmeSpatial getChild(Spatial spat) {
        if (spat == null) {
            return null;
        }
        if (getLookup().lookup(spat.getClass()) == spat) {
            return this;
        }

        Node[] children = getChildren().getNodes();
        for (int i = 0; i < children.length; i++) {
            Node node = children[i];
            if (node instanceof JmeSpatial) {
                JmeSpatial jmeSpatial = (JmeSpatial) node;
                JmeSpatial found = jmeSpatial.getChild(spat);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    //TODO: refresh does not work
    public void refresh(boolean immediate) {
        factory.refreshChildren(immediate);
    }

    protected SystemAction[] createActions() {
        return new SystemAction[]{
                    SystemAction.get(RenameAction.class),
                    SystemAction.get(CopyAction.class),
                    SystemAction.get(CutAction.class),
                    SystemAction.get(PasteAction.class),
                    SystemAction.get(DeleteAction.class)
                };
    }

    public void fireSave(boolean modified) {
        fireSave(modified, false);
    }

    public void fireSave(boolean modified, boolean recursive) {
        if (modified) {
            if (saveCookie != null) {
                lookupContents.remove(saveCookie);
                lookupContents.add(saveCookie);
            }
        } else {
            if (saveCookie != null) {
                lookupContents.remove(saveCookie);
            }
        }
        if (recursive) {
            Node[] children = getChildren().getNodes();
            for (int i = 0; i < children.length; i++) {
                Node node = children[i];
                if (node instanceof JmeSpatial) {
                    ((JmeSpatial) node).fireSave(modified, recursive);
                }
            }
        }
    }

    /**
     * @param saveCookie the saveCookie to set
     */
    public JmeSpatial setSaveCookie(SaveCookie saveCookie) {
        this.saveCookie = saveCookie;
//        if (saveCookie != null) {
//            lookupContents.add(saveCookie);
//        }
        return this;
    }

    private class SaveCookieImpl implements SaveCookie {

        public void save() throws IOException {
//            Confirmation msg = new NotifyDescriptor.Confirmation("This plugin can not save!",
//                    NotifyDescriptor.OK_CANCEL_OPTION,
//                    NotifyDescriptor.QUESTION_MESSAGE);
//
//            Object result = DialogDisplayer.getDefault().notify(msg);
            //When user clicks "Yes", indicating they really want to save,
            //we need to disable the Save button and Save menu item,
            //so that it will only be usable when the next change is made
            //to the text field:
//            if (NotifyDescriptor.YES_OPTION.equals(result)) {
//                fireSave(false);
            //Implement your save functionality here.
//            }
        }
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void setName(final String s) {
        super.setName(s);
        try {
            SceneApplication.getApplication().enqueue(new Callable<Void>() {

                public Void call() throws Exception {
                    spatial.setName(s);
                    return null;
                }
            }).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void destroy() throws IOException {
        try {
            SceneApplication.getApplication().enqueue(new Callable<Void>() {

                public Void call() throws Exception {
                    spatial.removeFromParent();
                    return null;
                }
            }).get();
            //TODO: not a good cast
            JmeNode node = ((JmeNode) getParentNode());
            if (node != null) {
                node.refresh(false);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        Transferable trans = new Transferable() {

            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{SPATIAL_FLAVOR};
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                if (SPATIAL_FLAVOR.equals(flavor)) {
                    return true;
                }
                return false;
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (SPATIAL_FLAVOR.equals(flavor)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    BinaryExporter.getInstance().save(spatial, out);

                    return new ClipboardSpatial(out.toByteArray());
                } else {
                    throw new UnsupportedFlavorException(flavor);
                }
            }
        };
        return trans;
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        Transferable trans = new Transferable() {

            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{SPATIAL_FLAVOR};
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                if (SPATIAL_FLAVOR.equals(flavor)) {
                    return true;
                }
                return false;
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (SPATIAL_FLAVOR.equals(flavor)) {
                    try {
                        SceneApplication.getApplication().enqueue(new Callable<Void>() {

                            public Void call() throws Exception {
                                spatial.removeFromParent();
                                return null;
                            }
                        }).get();
                        //TODO: not a good cast
                        JmeNode node = ((JmeNode) getParentNode());
                        if (node != null) {
                            node.refresh(false);
                        }
//                        return spatial;
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        BinaryExporter.getInstance().save(spatial, out);
//
                        return new ClipboardSpatial(out.toByteArray());
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return null;
                } else {
                    throw new UnsupportedFlavorException(flavor);
                }
            }
        };
        return trans;
    }

    /**
     * returns the PropertySet with the given name (mostly Class.name)
     * @param name
     * @return The PropertySet or null if no PropertySet by that name exists
     */
    public PropertySet getPropertySet(String name){
        for (int i = 0; i < getPropertySets().length; i++) {
            PropertySet propertySet = getPropertySets()[i];
            if(propertySet.getName().equals(name)){
                return propertySet;
            }
        }
        return null;
    }

    @Override
    protected Sheet createSheet() {
        //TODO: multithreading..
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("Spatial");
        set.setName(Spatial.class.getName());
        Spatial obj = spatial;//getLookup().lookup(Spatial.class);
        if (obj == null) {
            return sheet;
        }
//        set.put(makeProperty(obj, String.class, "getName", "setName", "name"));

        set.put(makeProperty(obj, int.class, "getVertexCount", "Vertexes"));
        set.put(makeProperty(obj, int.class, "getTriangleCount", "Triangles"));

//        set.put(makeProperty(obj, Transform.class,"getWorldTransform","world transform"));
        set.put(makeProperty(obj, Vector3f.class, "getWorldTranslation", "World Translation"));
        set.put(makeProperty(obj, Quaternion.class, "getWorldRotation", "World Totation"));
        set.put(makeProperty(obj, Vector3f.class, "getWorldScale", "World Scale"));

        set.put(makeProperty(obj, Vector3f.class, "getLocalTranslation", "setLocalTranslation", "Local Translation"));
        set.put(makeProperty(obj, Quaternion.class, "getLocalRotation", "setLocalRotation", "Local Rotation"));
        set.put(makeProperty(obj, Vector3f.class, "getLocalScale", "setLocalScale", "Local Scale"));

        set.put(makeProperty(obj, BoundingVolume.class, "getWorldBound", "World Bound"));

        set.put(makeProperty(obj, CullHint.class, "getCullHint", "setCullHint", "Cull Hint"));
        set.put(makeProperty(obj, CullHint.class, "getLocalCullHint", "Local Cull Hint"));
        set.put(makeProperty(obj, ShadowMode.class, "getShadowMode", "setShadowMode", "Shadow Mode"));
        set.put(makeProperty(obj, ShadowMode.class, "getLocalShadowMode", "Local Shadow Mode"));
        set.put(makeProperty(obj, LightList.class, "getWorldLightList", "World Light List"));

        set.put(makeProperty(obj, RenderQueue.Bucket.class, "getQueueBucket", "setQueueBucket", "Queue Bucket"));

        sheet.put(set);
        return sheet;

    }

    private Property makeProperty(Spatial obj, Class returntype, String method, String name) {
        Property prop = null;
        try {
            prop = new JmeProperty(obj, returntype, method, null);
            prop.setName(name);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }

    private Property makeProperty(Spatial obj, Class returntype, String method, String setter, String name) {
        Property prop = null;
        try {
            prop = new JmeProperty(obj, returntype, method, setter);
            prop.setName(name);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return prop;
    }

    public Class getExplorerObjectClass() {
        return Spatial.class;
    }

    public Class getExplorerNodeClass() {
        return JmeSpatial.class;
    }

    public Node[] createNodes(Object key, Object key2, SaveCookie cookie) {
        JmeChildren children=new JmeChildren((com.jme3.scene.Spatial)key);
        return new Node[]{new JmeSpatial((Spatial) key, children).setSaveCookie(cookie)};
    }

}
