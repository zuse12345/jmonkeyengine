/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.post.filters;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

/**
 *
 * @author nehon
 */
public class LightScatteringFilter extends Filter {

    private Vector3f lightPosition;
    private Vector3f screenLightPos=new Vector3f();
    private int nbSamples = 50;
    private float blurStart = 0.02f;
    private float blurWidth = 0.9f;
    private float lightDensity = 0.55f;
    private boolean adaptative=true;
    Vector3f viewLightPos=new Vector3f();
    private boolean display;    

    public LightScatteringFilter(Vector3f lightPosition) {
        super("Light Scattering");
        this.lightPosition = lightPosition;
       
    }

    @Override
    public boolean isRequiresDepthTexture() {
        return true;
    }

    @Override
    public Material getMaterial() {
        material.setVector3("m_LightPosition", screenLightPos);
        material.setInt("m_NbSamples", nbSamples);
        material.setFloat("m_BlurStart", blurStart);
        material.setFloat("m_BlurWidth", blurWidth);
        material.setFloat("m_LightDensity", lightDensity);
        material.setBoolean("m_Display", display);
        return material;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
        getClipCoordinates(lightPosition,screenLightPos,viewPort.getCamera());
      //  screenLightPos.x = screenLightPos.x / viewPort.getCamera().getWidth();
      //  screenLightPos.y = screenLightPos.y / viewPort.getCamera().getHeight();

        viewPort.getCamera().getViewMatrix().mult(lightPosition, viewLightPos);
        //System.err.println("viewLightPos "+viewLightPos);
        display=screenLightPos.x<1.6f && screenLightPos.x>-0.6f && screenLightPos.y<1.6f && screenLightPos.y>-0.6f && viewLightPos.z<0;
//System.err.println("camdir "+viewPort.getCamera().getDirection());
//System.err.println("lightPos "+lightPosition);
//System.err.println("screenLightPos "+screenLightPos);
        if(adaptative){
            lightDensity=1.4f-Math.max(screenLightPos.x, screenLightPos.y);
        }
    }

    public Vector3f getClipCoordinates( Vector3f worldPosition, Vector3f store,Camera cam ) {     

        float w = cam.getViewProjectionMatrix().multProj(worldPosition, store);
        store.divideLocal(w);

        store.x = ( ( store.x + 1f ) * ( cam.getViewPortRight() -  cam.getViewPortLeft() ) / 2f + cam.getViewPortLeft() );
        store.y = ( ( store.y + 1f ) * (  cam.getViewPortTop() -  cam.getViewPortBottom() ) / 2f + cam.getViewPortBottom() );
        store.z = ( store.z + 1f ) / 2f;

        return store;
    }

    @Override
    public void initMaterial(AssetManager manager) {
        material = new Material(manager, "Common/MatDefs/Light/LightScattering.j3md");
    }

    public float getBlurStart() {
        return blurStart;
    }

    public void setBlurStart(float blurStart) {
        this.blurStart = blurStart;
    }

    public float getBlurWidth() {
        return blurWidth;
    }

    public void setBlurWidth(float blurWidth) {
        this.blurWidth = blurWidth;
    }

    public float getLightDensity() {
        return lightDensity;
    }

    public void setLightDensity(float lightDensity) {
        this.lightDensity = lightDensity;
    }

    public Vector3f getLightPosition() {
        return lightPosition;
    }

    public void setLightPosition(Vector3f lightPosition) {
        this.lightPosition = lightPosition;
    }

    public int getNbSamples() {
        return nbSamples;
    }

    public void setNbSamples(int nbSamples) {
        this.nbSamples = nbSamples;
    }
}
