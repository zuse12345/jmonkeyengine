/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.app.state.impl;

import com.g3d.app.state.AppStateManager;
import com.g3d.app.state.AssetAppState;
import com.g3d.app.state.GuiAppState;
import com.g3d.app.state.RenderAppState;
import com.g3d.font.BitmapFont;
import com.g3d.font.BitmapText;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Node;

public class DebugGuiAppState implements GuiAppState {

    private Node guiNode = new Node("GUI Node");
    private BitmapText fpsText;

    public Node getGuiNode() {
        return guiNode;
    }

    public String getName(){
        return "Debug GUI";
    }

    public void notifyAttach(AppStateManager manager) {
        AssetAppState assetService = manager.getService(AssetAppState.class);
        BitmapFont font = assetService.getAssetManager().loadFont("cooper.fnt");
        fpsText = new BitmapText(font, false);
        fpsText.setSize(font.getCharSet().getRenderedSize());
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(fpsText);
    }

    public void update(AppStateManager manager, float tpf) {
        guiNode.updateLogicalState(tpf);
        guiNode.updateGeometricState();

        RenderAppState renderService = manager.getService(RenderAppState.class);
        renderService.getRenderer().addToQueue(fpsText, Bucket.Gui);
    }

    public void notifyDetach(AppStateManager manager) {
    }

}
