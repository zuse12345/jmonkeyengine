package com.g3d.app.state.impl;

import com.g3d.app.state.AppStateManager;
import com.g3d.app.state.SceneAppState;
import com.g3d.scene.Node;

public class SimpleSceneAppState implements SceneAppState {

    private Node rootNode = new Node("Root Node");

    public SimpleSceneAppState(){
    }

    public Node getRootNode() {
        return rootNode;
    }

    public String getName() {
        return "Simple Scene";
    }

    public void notifyAttach(AppStateManager manager) {
    }

    public void notifyDetach(AppStateManager manager) {
    }

    public void update(AppStateManager manager, float tpf) {
        rootNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
    }

}
