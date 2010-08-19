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


/**
 *
 * @author nehon
 */
public class RadialBlurFilter extends Filter {

    private float sampleDist = 1.0f;
    private float sampleStrength = 2.2f;

    public RadialBlurFilter() {
        super("Radial blur");
    }

    public RadialBlurFilter( float sampleDist,float sampleStrength) {
        this();
        this.sampleDist=sampleDist;
        this.sampleStrength=sampleStrength;
    }

    @Override
    public Material getMaterial() {
             
        material.setFloat("m_SampleDist", sampleDist);
        material.setFloat("m_SampleStrength", sampleStrength);
        return material;
    }

    @Override
    public void preRender( RenderManager renderManager,ViewPort viewPort) {

    }

    public float getSampleDist() {
        return sampleDist;
    }

    public void setSampleDist(float sampleDist) {
        this.sampleDist = sampleDist;
    }

    public float getSampleStrength() {
        return sampleStrength;
    }

    public void setSampleStrength(float sampleStrength) {
        this.sampleStrength = sampleStrength;
    }

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/Blur/RadialBlur.j3md");
    }



}
