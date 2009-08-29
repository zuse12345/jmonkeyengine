package com.g3d.app.state.impl;

import com.g3d.app.state.AppStateManager;
import com.g3d.app.state.AssetAppState;
import com.g3d.asset.AssetManager;

public class DefaultAssetAppState implements AssetAppState {

    protected AssetManager assetManager;

    public DefaultAssetAppState(){
        assetManager = new AssetManager(true);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public String getName() {
        return "Default Asset";
    }

    public void notifyAttach(AppStateManager manager) {
    }

    public void notifyDetach(AppStateManager manager) {
        assetManager.clearCache();
    }

    public void update(AppStateManager manager, float tpf) {
    }

}
