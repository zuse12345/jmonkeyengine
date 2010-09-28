/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.post.filters;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.post.Filter.Pass;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.Image.Format;

public class CartoonEdgeFilter extends Filter {

    private Pass normalPass;
    private Material normalMaterial;

    public CartoonEdgeFilter() {
        normalPass = new Pass();
        setRequiresDepthTexture(true);
    }

    @Override
    public void init(AssetManager manager, int width, int height) {
        super.init(manager, width, height);
        normalPass.init(width, height, Format.RGB8, Format.Depth);
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
        Renderer r = renderManager.getRenderer();
        r.setFrameBuffer(normalPass.getRenderFrameBuffer());
        renderManager.getRenderer().clearBuffers(true, true, true);
        renderManager.setForcedMaterial(normalMaterial);
        renderManager.renderViewPortQueues(viewPort, false);
        renderManager.setForcedMaterial(null);
        renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());

    }

    @Override
    public Material getMaterial() {
        material.setTexture("m_NormalsTexture", normalPass.getRenderedTexture());
        return material;
    }

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/Post/CartoonEdge.j3md");
        normalMaterial = new Material(manager, "Common/MatDefs/SSAO/normal.j3md");
    }

}
