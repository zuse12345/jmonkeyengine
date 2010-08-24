/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.water;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.shader.Shader;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;

/**
 *
 * @author normenhansen
 */
public class SimpleWaterProcessor implements SceneProcessor {

    RenderManager rm;
    ViewPort vp;
    Camera reflectionCam;
    Camera refractionCam;
    Texture2D reflectionTexture = new Texture2D(512, 512, Format.RGB8);
    Texture2D refractionTexture = new Texture2D(512, 512, Format.RGB8);
    Texture2D depthTexture = new Texture2D(512, 512, Format.RGB8);
    Texture2D normalTexture = new Texture2D(512, 512, Format.RGB8);
    Spatial reflectionScene;
    float waterHeight;
    Plane plane = new Plane(Vector3f.UNIT_Y, 1);
    Ray ray = new Ray();
    Vector3f targetLocation = new Vector3f();
    AssetManager manager;
    Material material;

    public SimpleWaterProcessor(AssetManager manager) {
        this.manager = manager;
        material = new Material(manager, "Common/MatDefs/Water/SimpleWater.j3md");
        applyTextures(material);
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        this.rm = rm;
        this.vp = vp;
        reflectionCam = new Camera(512, 512);
        refractionCam = new Camera(512, 512);
        createReflectionView();
        createRefractionView();
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return rm != null;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
        Shader sh=new Shader();
        Camera sceneCam = rm.getCurrentCamera();
        //update ray
        ray.setOrigin(sceneCam.getLocation());
        ray.setDirection(sceneCam.getDirection());

        //update refraction cam
        refractionCam.setLocation(sceneCam.getLocation());
        refractionCam.setRotation(sceneCam.getRotation());

        //update reflection cam
        ray.intersectsWherePlane(plane, targetLocation);
        reflectionCam.setLocation(plane.reflect(sceneCam.getLocation(), new Vector3f()));
//        reflectionCam.setAxes(Vector3f.UNIT_X.negate(), Vector3f.UNIT_Y.negate(), Vector3f.UNIT_Z.negate());
        reflectionCam.lookAt(targetLocation, Vector3f.UNIT_Y);

        material.setColor("m_viewpos", new ColorRGBA(sceneCam.getLocation().x, sceneCam.getLocation().y, sceneCam.getLocation().z, 1.0f));

//        material.setMatrix4("", Matrix4f.IDENTITY);
    }

    public void postFrame(FrameBuffer out) {
    }

    public void cleanup() {
    }

    public Material getMaterial() {
        return material;
    }

    public void setReflectionScene(Spatial spat) {
        reflectionScene = spat;
    }

    protected void applyTextures(Material mat) {
        mat.setTexture("m_water_reflection", reflectionTexture);
        mat.setTexture("m_water_refraction", refractionTexture);
        mat.setTexture("m_water_normalmap", normalTexture);
        mat.setTexture("m_water_depthmap", depthTexture);
    }

    protected void createReflectionView() {
        // create a pre-view. a view that is rendered before the main view
        ViewPort offView = rm.createPreView("Reflection View", reflectionCam);
        offView.setClearEnabled(true);
        offView.setBackgroundColor(ColorRGBA.Black);
        // create offscreen framebuffer
        FrameBuffer offBuffer = new FrameBuffer(512, 512, 0);
        //setup framebuffer's cam
        reflectionCam.setFrustumPerspective(45f, 1f, 1f, 1000f);
        //setup framebuffer's texture
        //setup framebuffer to use texture
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorTexture(reflectionTexture);
        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);
        offView.addProcessor(new SimpleWaterReflectionProcessor());
        // attach the scene to the viewport to be rendered
        offView.attachScene(reflectionScene);
    }

    protected void createRefractionView() {
        // create a pre-view. a view that is rendered before the main view
        ViewPort offView = rm.createPreView("Refraction View", refractionCam);
        offView.setClearEnabled(true);
        offView.setBackgroundColor(ColorRGBA.Black);
        // create offscreen framebuffer
        FrameBuffer offBuffer = new FrameBuffer(512, 512, 0);
        //setup framebuffer's cam
        refractionCam.setFrustumPerspective(45f, 1f, 1f, 1000f);
        //setup framebuffer's texture
        //setup framebuffer to use texture
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorTexture(refractionTexture);
        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);
        offView.addProcessor(new SimpleWaterRefractionProcessor());
        // attach the scene to the viewport to be rendered
        offView.attachScene(reflectionScene);
    }
}
