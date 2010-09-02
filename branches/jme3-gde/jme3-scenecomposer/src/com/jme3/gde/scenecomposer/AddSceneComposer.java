/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.SpatialAssetDataObject;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class AddSceneComposer implements ActionListener {

    private final SpatialAssetDataObject context;

    public AddSceneComposer(SpatialAssetDataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        ProjectAssetManager manager=context.getLookup().lookup(ProjectAssetManager.class);
        if(manager == null) return;
        SceneComposerTopComponent composer=SceneComposerTopComponent.findInstance();
        composer.addModel(context);
    }
}
