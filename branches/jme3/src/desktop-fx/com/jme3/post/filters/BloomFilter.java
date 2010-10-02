/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.post.filters;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import java.util.ArrayList;

/**
 *
 * @author Nehon
 */
public class BloomFilter extends Filter{

    private Pass pass32=new Pass();
    private Pass pass64=new Pass();
    private Pass pass128=new Pass();
    private Pass pass256=new Pass();
    private MinFilter fbMinFilter = MinFilter.BilinearNoMipMaps;
    private MagFilter fbMagFilter = MagFilter.Bilinear;
    private int screenWidth;
    private int screenHeight;
    
    public BloomFilter(int width,int height) {
        super("Bloom");
        screenWidth=width;
        screenHeight=height;
    }

    @Override
    public void initMaterial(AssetManager manager) {
       material = new Material(manager, "Common/MatDefs/Post/BloomFinal.j3md");
       postRenderPasses=new ArrayList<Pass>();

       initPass(manager, screenWidth/16,screenHeight/16, pass32);
       initPass(manager, screenWidth/8,screenHeight/8, pass64);
       initPass(manager, screenWidth/4,screenHeight/4, pass128);
       initPass(manager,screenWidth/2,screenHeight/2, pass256);

    }

    private void initPass(AssetManager manager,int width,int height,Pass pass) {
        Material postMaterial = new Material(manager, "Common/MatDefs/Post/Bloom.j3md");
        postMaterial.setFloat("m_Size", height);
        pass.init(width, height, Format.RGBA8, Format.Depth, postMaterial);
        pass.getRenderedTexture().setMagFilter(fbMagFilter);
        pass.getRenderedTexture().setMinFilter(fbMinFilter);
        postRenderPasses.add(pass);

    }

    @Override
    public Material getMaterial() {
        material.setTexture("m_Tex32", pass32.getRenderedTexture());
        material.setTexture("m_Tex64", pass64.getRenderedTexture());
        material.setTexture("m_Tex128", pass128.getRenderedTexture());
        material.setTexture("m_Tex256", pass256.getRenderedTexture());
        return material;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {

    }


}
