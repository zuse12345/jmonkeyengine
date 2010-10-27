/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.assetpack;

import com.jme3.asset.AssetKey;
import com.jme3.gde.assetpack.actions.AddAssetAction;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author normenhansen
 */
public class AssetPackLoader {

    public static Spatial loadAssetPackModel(String name, NodeList fileNodeList, ProjectAssetManager pm) {
        AssetKey<Spatial> key = null;
        Material mat = null;
        Spatial model;
        //TODO: mesh.xml!!
        if (hasExtension(name, "xml")) {
            for (int i = 0; i < fileNodeList.getLength(); i++) {
                Element fileElem = (Element) fileNodeList.item(i);
                String type = fileElem.getAttribute("type");
                String path = fileElem.getAttribute("path");
                if ("material".equals(type)) {
                    if (hasExtension(path, "j3m")) {
                        mat = pm.getManager().loadMaterial(path);
                    } else if (hasExtension(path, "material")) {
                        key = new OgreMeshKey(name, (MaterialList) pm.getManager().loadAsset(path));
                    }
                }
            }
        } else if (hasExtension(name, "scene")) {
            for (int i = 0; i < fileNodeList.getLength(); i++) {
                Element fileElem = (Element) fileNodeList.item(i);
                String type = fileElem.getAttribute("type");
                String path = fileElem.getAttribute("path");
                if ("material".equals(type)) {
                    if (hasExtension(path, "j3m")) {
                        mat = pm.getManager().loadMaterial(path);
                    } else if (hasExtension(path, "material")) {
                        key = new OgreMeshKey(name, (MaterialList) pm.getManager().loadAsset(path));
                    }
                }
            }
        } else if (hasExtension(name, "obj")) {
            for (int i = 0; i < fileNodeList.getLength(); i++) {
                Element fileElem = (Element) fileNodeList.item(i);
                String type = fileElem.getAttribute("type");
                String path = fileElem.getAttribute("path");
                if ("material".equals(type)) {
                    if (hasExtension(path, "j3m")) {
                        mat = pm.getManager().loadMaterial(path);
                    }
                }
            }
        } else if (hasExtension(name, "j3o")) {
            //should have all info inside
        }
        if (key != null && mat != null) {
            Logger.getLogger(AddAssetAction.class.getName()).log(Level.WARNING, "j3m and ogre material defined for asset {0}.", name);
        }
        if (key != null) {
            model = pm.getManager().loadAsset(key);
        } else {
            model = pm.getManager().loadModel(name);
        }
        if (model == null) {
            Logger.getLogger(AddAssetAction.class.getName()).log(Level.SEVERE, "Could not load model {0}!", name);
            return null;
        }
        if (mat != null) {
            model.setMaterial(mat);
        }
        return model;
    }

    public static boolean hasExtension(String name, String extension) {
        int idx = name.lastIndexOf(".");
        if (idx < 0) {
            return false;
        }
        String ext = name.substring(idx + 1, name.length());
        if (ext.equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }
}
