/*
 * Copyright (c) 2009-2010 jMonkeyEngine
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
package com.jme3.gde.core.assets;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author normenhansen
 */
public class ProjectAssetManager {

    private Project project;
    private AssetManager manager;
    private List<String> folderName = new LinkedList<String>();

    public ProjectAssetManager(Project prj, String folderName) {
        this(prj);
        addFileLocator(folderName);
    }

    public ProjectAssetManager(Project prj) {
        this.project = prj;
        AssetManager manager = getManager();
        for (AssetManagerConfigurator di : Lookup.getDefault().lookupAll(AssetManagerConfigurator.class)) {
            di.prepareManager(manager);
        }
    }

    public void addFileLocator(String relativePath) {
        String string = project.getProjectDirectory().getPath() + "/" + relativePath + "/";
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Add locator:{0}", string);
        manager.registerLocator(string,
                "com.jme3.asset.plugins.FileLocator");
        folderName.add(relativePath);
    }

    public Project getProject() {
        return project;
    }

    public String getRelativeAssetPath(String absolutePath) {
        String prefix = project.getProjectDirectory().getFileObject(getFolderName() + "/").getPath();
        int idx = absolutePath.indexOf(prefix);
        if (idx == 0) {
            return absolutePath.substring(prefix.length() + 1);
        }
        return absolutePath;
    }

    public AssetManager getManager() {
        if (manager == null) {
            manager = new DesktopAssetManager(true);
        }
        return manager;
    }

    public String[] getMaterials() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(getFolderName() + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("j3m")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getSounds() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(getFolderName() + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("wav") || asset.hasExt("ogg")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getTextures() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(getFolderName() + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("jpg") || asset.hasExt("jpeg") || asset.hasExt("gif") || asset.hasExt("png") || asset.hasExt("dds") || asset.hasExt("pfm") || asset.hasExt("hdr") || asset.hasExt("tga")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getMatDefs() {
        FileObject assetsFolder = project.getProjectDirectory().getFileObject(getFolderName() + "/");
        Enumeration<FileObject> assets = (Enumeration<FileObject>) assetsFolder.getChildren(true);
        ArrayList<String> list = new ArrayList<String>();
        while (assets.hasMoreElements()) {
            FileObject asset = assets.nextElement();
            if (asset.hasExt("j3md")) {
                list.add(getRelativeAssetPath(asset.getPath()));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * @return the folderName
     */
    public String getFolderName() {
        return folderName.get(0);
    }

    /**
     * @return the folderName
     */
    public String getAssetFolderName() {
        return project.getProjectDirectory().getPath() + "/" + getFolderName();
    }

    public String getAbsoluteAssetPath(String path) {
        for (Iterator<String> it = folderName.iterator(); it.hasNext();) {
            String string = project.getProjectDirectory().getPath() + "/" + it.next() + "/" + path;
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Check {0}", string);
            File file = new File(string);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * @param folderName the folderName to set
     */
    public void setFolderName(String folderName) {
        if (folderName.length() > 0) {
            this.folderName.remove(0);
        }
        this.folderName.add(0, folderName);
    }
}
