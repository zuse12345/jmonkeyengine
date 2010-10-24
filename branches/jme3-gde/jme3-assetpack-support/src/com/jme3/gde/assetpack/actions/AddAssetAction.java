/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.assetpack.actions;

import com.jme3.gde.assetpack.XmlHelper;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.scene.Spatial;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.w3c.dom.Element;
import com.jme3.gde.scenecomposer.SceneComposerTopComponent;
import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.NodeList;

public final class AddAssetAction implements Action {

    private final Node context;

    public AddAssetAction(Node context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        ProjectAssetManager pm = context.getLookup().lookup(ProjectAssetManager.class);
        if (pm == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "AssetManager not found!");
            return;
        }
        Element assetElement = context.getLookup().lookup(Element.class);
        String type = assetElement.getAttribute("type");
        if ("model".equals(type)) {
            if (addModelToScene(assetElement, pm)) {
                return;
            }
        } else {
            if (addFiles(assetElement, pm)) {
                return;
            }
        }
    }

    private boolean addFiles(Element assetElement, ProjectAssetManager pm) {
        NodeList list = assetElement.getElementsByTagName("file");
        //TODO: not good :/
        ProjectAssetManager proman = null;
        try {
            proman = SceneApplication.getApplication().getCurrentSceneRequest().getManager();
            if (proman == null) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not get project asset manager!");
                return true;
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not get project asset manager!");
            return true;
        }
        for (int i = 0; i < list.getLength(); i++) {
            Element fileElem = (Element) list.item(i);
            try {
                String src = pm.getAbsoluteAssetPath(fileElem.getAttribute("path"));
                if (src == null) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not find texture with manager!");
                    return true;
                }
                FileObject srcFile = FileUtil.toFileObject(new File(src));
                String destName = proman.getAssetFolderName() + "/" + fileElem.getAttribute("path");
                String destFolder = destName.replace("\\", "/");
                destFolder = destFolder.substring(0, destFolder.lastIndexOf("/"));
                FileObject folder = FileUtil.createFolder(new File(destFolder));
                srcFile.copy(folder, srcFile.getName(), srcFile.getExt());
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not copy texture: {0}", ex.getMessage());
            }
        }
        return false;
    }

    private boolean addModelToScene(Element assetElement, ProjectAssetManager pm) {
        Element fileElement = XmlHelper.findChildElementWithAttribute(assetElement, "file", "main", "true");
        if (fileElement == null) {
            fileElement = XmlHelper.findChildElement(assetElement, "file");
        }
        if (fileElement == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not find main file in asset description!");
            return true;
        }
        String name = fileElement.getAttribute("path");
        if (name == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not find path for file!");
            return true;
        }
        Spatial model = pm.getManager().loadModel(name);
        if (model == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not load model {0}!", name);
            return true;
        }
        NodeList list = assetElement.getElementsByTagName("file");
        //TODO: not good :/
        ProjectAssetManager proman = null;
        try {
            proman = SceneApplication.getApplication().getCurrentSceneRequest().getManager();
            if (proman == null) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not get project asset manager!");
                return true;
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not get project asset manager!");
            return true;
        }
        for (int i = 0; i < list.getLength(); i++) {
            Element fileElem = (Element) list.item(i);
            if ("texture".equals(fileElem.getAttribute("type"))) {
                try {
                    String src = pm.getAbsoluteAssetPath(fileElem.getAttribute("path"));
                    if (src == null) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not find texture with manager!");
                        return true;
                    }
                    FileObject srcFile = FileUtil.toFileObject(new File(src));
                    String destName = proman.getAssetFolderName() + "/" + fileElem.getAttribute("path");
                    String destFolder = destName.replace("\\", "/");
                    destFolder = destFolder.substring(0, destFolder.lastIndexOf("/"));
                    FileObject folder = FileUtil.createFolder(new File(destFolder));
                    srcFile.copy(folder, srcFile.getName(), srcFile.getExt());
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not copy texture: {0}", ex.getMessage());
                }
            }
        }
        SceneComposerTopComponent.findInstance().addModel(model);
        return false;
    }

    public Object getValue(String key) {
        if (key.equals(NAME)) {
            return "Add to SceneComposer..";
        }
        return null;
    }

    public void putValue(String key, Object value) {
    }

    public void setEnabled(boolean b) {
    }

    public boolean isEnabled() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}
