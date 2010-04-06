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
package com.jme3.gde.core.scene.nodes;

import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class JmeSpatialChildFactory extends ChildFactory<Spatial> {

    private com.jme3.scene.Spatial spatial;

    public JmeSpatialChildFactory(com.jme3.scene.Spatial spatial) {
        this.spatial = spatial;
    }

    @Override
    protected boolean createKeys(final List<Spatial> toPopulate) {
        try {
            return SceneApplication.getApplication().enqueue(new Callable<Boolean>() {

                public Boolean call() throws Exception {
                    if (spatial != null && spatial instanceof com.jme3.scene.Node) {
                        toPopulate.addAll(((com.jme3.scene.Node) spatial).getChildren());
                        return true;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Spatial key) {
        //TODO: add way for plugins to add their own spatial types, probably
        //      best via registering some object in the global lookup
        JmeSpatialChildFactory factory = new JmeSpatialChildFactory(key);
        if (key instanceof com.jme3.audio.AudioNode) {
            return new JmeAudioNode((com.jme3.audio.AudioNode) key, Children.create(factory, false));
        }
        if (key instanceof com.jme3.scene.Node) {
            return new JmeNode((com.jme3.scene.Node) key, Children.create(factory, false));
        }
        if (key instanceof com.jme3.scene.Geometry) {
            return new JmeGeometry((Geometry) key, Children.create(factory, false));
        }
        return new JmeSpatial(key, Children.create(factory, false));
    }

    @Override
    protected Node[] createNodesForKey(Spatial key) {
        JmeSpatialChildFactory factory = new JmeSpatialChildFactory(key);
        Node[] nodes = new Node[1];
        if (key instanceof com.jme3.audio.AudioNode) {
            nodes[0] = new JmeAudioNode((com.jme3.audio.AudioNode) key, Children.create(factory, false));
        }
        else if (key instanceof com.jme3.scene.Node) {
            nodes[0] = new JmeNode((com.jme3.scene.Node) key, Children.create(factory, false));
        }
        else if (key instanceof com.jme3.scene.Geometry) {
            nodes[0] = new JmeGeometry((Geometry) key, Children.create(factory, false));
        }
        else {
            nodes[0] = new JmeSpatial(key, Children.create(factory, false));
        }
        return nodes;
    }
}
