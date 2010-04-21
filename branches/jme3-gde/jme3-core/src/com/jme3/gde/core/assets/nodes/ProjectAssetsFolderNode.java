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
import java.awt.Image;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author normenhansen
 */
public class ProjectAssetsFolderNode extends FilterNode {

    private Image smallImage;
    private String name;
    private Node node;
    private ProjectAssetManager manager;

    public ProjectAssetsFolderNode(ProjectAssetManager manager, Node node, String icon, String name) {
        super(node, Children.create(new AssetNodeChildFactory(node, manager), true), createLookupProxy(manager, node));
        this.manager = manager;
        this.name = name;
        this.node = node;
        smallImage = ImageUtilities.loadImage("/com/jme3/gde/core/assets/nodes/icons/" + icon + ".gif");
    }

    public String getDisplayName() {
        return name;
    }

//    public Image getIcon(int type) {
//        Image original = node.getIcon(type);
//        return ImageUtilities.mergeImages(original, smallImage, 5, 5);
//    }
//
//    public Image getOpenedIcon(int type) {
//        Image original = node.getOpenedIcon(type);
//        return ImageUtilities.mergeImages(original, smallImage, 5, 5);
//    }

    public static Lookup createLookupProxy(ProjectAssetManager manager, Node node) {
        return new ProxyLookup(
                new Lookup[]{
                    node.getLookup(),
                    Lookups.fixed(manager)
                });
    }
}
