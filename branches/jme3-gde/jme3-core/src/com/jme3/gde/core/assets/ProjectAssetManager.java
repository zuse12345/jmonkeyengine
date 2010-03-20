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
import com.jme3.system.JmeSystem;
import org.netbeans.api.project.Project;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author normenhansen
 */
public class ProjectAssetManager {
    private Project project;
    //TODO: one assetmanager or multiple?
    private static AssetManager manager;

    public ProjectAssetManager(Project prj) {
        this.project=prj;
        AssetManager manager=getManager();
        StatusDisplayer.getDefault().setStatusText("adding folders from "+prj.getProjectDirectory()+" to assetmanager");
//        manager =  G3DSystem.newAssetManager();
//        manager.registerLocator(prj.getProjectDirectory()+"/assets/fonts/",
//                "com.jme3.asset.plugins.FileSystemLocator", "fnt");
//        manager.registerLocator(prj.getProjectDirectory()+"/assets/materials/",
//                "com.jme3.asset.plugins.FileSystemLocator", "j3m");
        manager.registerLocator(prj.getProjectDirectory()+"/assets/models/",
                "com.jme3.asset.plugins.FileLocator", "j3o");
//        manager.registerLocator(prj.getProjectDirectory()+"/assets/ogremodels/",
//                "com.jme3.asset.plugins.FileSystemLocator", "meshxml, material");
//        manager.registerLocator(prj.getProjectDirectory()+"/assets/shaderlib/",
//                "com.jme3.asset.plugins.FileSystemLocator", "glsllib");
//        manager.registerLocator(prj.getProjectDirectory()+"/assets/shaders/",
//                "com.jme3.asset.plugins.FileSystemLocator", "glsl, vert, frag");
//        manager.registerLocator(prj.getProjectDirectory()+"/assets/sounds/",
//                "com.jme3.asset.plugins.FileSystemLocator", "wav, ogg, spx");
//        manager.registerLocator(prj.getProjectDirectory()+"/assets/textures/",
//                "com.jme3.asset.plugins.FileSystemLocator", "dds, hdr, pfm, tga, bmp, png, jpg, jpeg, gif");
    }

    public Project getProject() {
        return project;
    }

    public static void setManager(AssetManager _manager) {
        manager=_manager;
    }

    public static AssetManager getManager() {
        if(manager==null) manager=JmeSystem.newAssetManager();//new DesktopAssetManager(true);
        return manager;
    }



}