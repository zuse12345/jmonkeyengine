/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.core.scene.nodes.JmeNode;
import com.jme3.gde.core.scene.nodes.NodeUtility;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public final class OpenSceneComposer implements ActionListener {

    private final DataObject context;

    public OpenSceneComposer(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        Runnable call = new Runnable() {

            public void run() {
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Opening in SceneComposer");
                progressHandle.start();
                final ProjectAssetManager manager = context.getLookup().lookup(ProjectAssetManager.class);
                if (manager == null) {
                    return;
                }
                final FileObject file = context.getPrimaryFile();
                String assetName = manager.getRelativeAssetPath(file.getPath());
                ((DesktopAssetManager) manager.getManager()).clearCache();
                final Spatial spat = manager.getManager().loadModel(assetName);
                if (spat instanceof Node) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            JmeNode jmeNode = NodeUtility.createNode((Node) spat);
                            SceneComposerTopComponent composer = SceneComposerTopComponent.findInstance();
                            SceneRequest request = new SceneRequest(composer, jmeNode, manager);
                            composer.loadRequest(request, file);
                        }
                    });
                } else {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            Node node = new Node();
                            node.attachChild(spat);
                            JmeNode jmeNode = NodeUtility.createNode(node);
                            SceneComposerTopComponent composer = SceneComposerTopComponent.findInstance();
                            SceneRequest request = new SceneRequest(composer, jmeNode, manager);
                            composer.loadRequest(request, file);
                        }
                    });
                }
                progressHandle.finish();
            }
        };
        new Thread(call).start();
    }
}
