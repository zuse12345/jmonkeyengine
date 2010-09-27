/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.terraineditor;

import com.jme3.gde.core.scene.controller.AbstractCameraController;
import com.jme3.gde.core.sceneexplorer.nodes.JmeNode;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;

/**
 *
 * @author normenhansen
 */
public class TerrainCameraController extends AbstractCameraController {

    private JmeNode rootNode;

    public TerrainCameraController(Camera cam, InputManager inputManager, JmeNode rootNode) {
        super(cam, inputManager);
        this.rootNode = rootNode;
    }

    @Override
    protected void checkClick(int button) {
    }
}
