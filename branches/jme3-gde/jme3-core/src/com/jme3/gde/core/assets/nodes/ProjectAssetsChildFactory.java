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
package com.jme3.gde.core.assets.nodes;

import com.jme3.gde.core.assets.ProjectAssetManager;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class ProjectAssetsChildFactory extends ChildFactory<Node> {

    private Project project;
    private ProjectAssetManager manager;

    public ProjectAssetsChildFactory(Project project, ProjectAssetManager manager) {
        this.project = project;
        this.manager = manager;
    }

    @Override
    protected boolean createKeys(List<Node> toPopulate) {
        try {
            DataObject assetsFolder = DataObject.find(project.getProjectDirectory().getFileObject("assets"));
            Node node = assetsFolder.getNodeDelegate();
            Children children = node.getChildren();

            Enumeration<Node> nodes = children.nodes();
            while (nodes.hasMoreElements()) {
                Node child = nodes.nextElement();
                DataObject data = child.getLookup().lookup(DataObject.class);
                if (data != null) {
                    if (data.getPrimaryFile().getName().equals("textures")) {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "image", "textures"));
                    } else if (data.getPrimaryFile().getName().equals("models")) {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "model", "models"));
                    } else if (data.getPrimaryFile().getName().equals("scenes")) {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "scene", "scenes"));
                    } else if (data.getPrimaryFile().getName().equals("sounds")) {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "sound", "sounds"));
                    } else if (data.getPrimaryFile().getName().equals("fonts")) {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "font", "fonts"));
                    } else if (data.getPrimaryFile().getName().equals("gui")) {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "gui", "gui"));
                    } else if (data.getPrimaryFile().getName().equals("materials")) {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "material", "materials"));
                    } else {
                        toPopulate.add(new ProjectAssetsFolderNode(manager, child, "assets", data.getPrimaryFile().getName()));
                    }
                } else {
                    toPopulate.add(child);
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Node key) {
        return key;
    }

    @Override
    protected Node[] createNodesForKey(Node key) {
        return new Node[]{key};
    }
}
