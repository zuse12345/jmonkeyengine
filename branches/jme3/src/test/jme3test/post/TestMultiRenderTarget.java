/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.TangentBinormalGenerator;

public class TestMultiRenderTarget extends SimpleApplication implements SceneProcessor {

    private FrameBuffer fb;
    private Texture2D diffuseData, normalData, specularData, depthData;
    private Geometry sphere;
    private Picture display1, display2, display3, display4;
    
    private Picture display;
    private Material mat;

    float angle;
    PointLight pl;
    Spatial lightMdl;

    public static void main(String[] args){
        TestMultiRenderTarget app = new TestMultiRenderTarget();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.addProcessor(this);

//        flyCam.setEnabled(false);
//        cam.setLocation(new Vector3f(1.8324817f, 0.23943897f, 1.9401197f));
//        cam.setRotation(new Quaternion(-0.0181917f, 0.92379856f, -0.044238973f, -0.37987918f));

        Sphere sphMesh = new Sphere(32, 32, 1);
        sphMesh.setTextureMode(Sphere.TextureMode.Projected);
        sphMesh.updateGeometry(32, 32, 1, false, false);
        TangentBinormalGenerator.generate(sphMesh);

        sphere = new Geometry("Rock Ball", sphMesh);
        Material material = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.j3m");
        material.selectTechnique("GBuf");
        sphere.setMaterial(material);
        rootNode.attachChild(sphere);

        pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        pl.setPosition(new Vector3f(4f, 0f, 0f));
        guiNode.addLight(pl);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        lightMdl.setQueueBucket(Bucket.Opaque);
        guiNode.attachChild(lightMdl);

//        DirectionalLight dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(1,-1,1).normalizeLocal());
//        dl.setColor(new ColorRGBA(0.22f, 0.15f, 0.1f, 1.0f));
//        guiNode.addLight(dl);

        display1 = new Picture("Picture");
        display1.move(0, 0, -1); // make it appear behind stats view
//        display2 = (Picture) display1.clone();
//        display3 = (Picture) display1.clone();
//        display4 = (Picture) display1.clone();
        display  = (Picture) display1.clone();
//        display.setQueueBucket(Bucket.Opaque);
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
        viewPort.setOutputFrameBuffer(fb);
        guiViewPort.setClearEnabled(true);
        guiNode.attachChild(display);
//        guiNode.attachChild(display1);
//        guiNode.attachChild(display2);
//        guiNode.attachChild(display3);
//        guiNode.attachChild(display4);
        guiNode.updateGeometricState();
    }

    public void reshape(ViewPort vp, int w, int h) {
        diffuseData  = new Texture2D(w, h, Format.RGBA16F);
        normalData   = new Texture2D(w, h, Format.RGBA16F);
        specularData = new Texture2D(w, h, Format.RGBA16F);
        depthData    = new Texture2D(w, h, Format.Depth);

//        float farY = cam.getFrustumTop();
//        float farX = cam.getFrustumRight();
       
        mat = new Material(assetManager, "Common/MatDefs/Light/Deferred.j3md");
        mat.setTexture("m_DiffuseData",  diffuseData);
        mat.setTexture("m_SpecularData", specularData);
        mat.setTexture("m_NormalData",   normalData);
        mat.setTexture("m_DepthData",    depthData);
//        mat.setVector2("m_FrustumNearFar", new Vector2f(cam.getFrustumNear(), cam.getFrustumFar()));
//        mat.setVector3("m_FrustumCorner",  new Vector3f(farX, farY, vp.getCamera().getFrustumFar()));

        display.setMaterial(mat);
        display.setPosition(0, 0);
        display.setWidth(w);
        display.setHeight(h);
        
//        display1.setTexture(assetManager, diffuseData, false);
//        display2.setTexture(assetManager, normalData, false);
//        display3.setTexture(assetManager, specularData, false);
//        display4.setTexture(assetManager, depthData, false);
//
//        display1.setPosition(0, 0);
//        display2.setPosition(w/2, 0);
//        display3.setPosition(0, h/2);
//        display4.setPosition(w/2, h/2);
//
//        display1.setWidth(w/2);
//        display1.setHeight(h/2);
//
//        display2.setWidth(w/2);
//        display2.setHeight(h/2);
//
//        display3.setWidth(w/2);
//        display3.setHeight(h/2);
//
//        display4.setWidth(w/2);
//        display4.setHeight(h/2);

        guiNode.updateGeometricState();
        
        fb = new FrameBuffer(w, h, 0);
        fb.setDepthTexture(depthData);
        fb.addColorTexture(diffuseData);
        fb.addColorTexture(normalData);
        fb.addColorTexture(specularData);
        fb.setMultiTarget(true);
    }

    public boolean isInitialized() {
        return diffuseData != null;
    }

    public void preFrame(float tpf) {
        Matrix4f view = cam.getViewMatrix();
        Matrix4f proj = cam.getProjectionMatrix();
        mat.setMatrix4("m_ViewMatrix", view);
        mat.setMatrix4("m_ProjectionMatrixInverse", proj.invert());
    }

    public void postQueue(RenderQueue rq) {
    }

    public void postFrame(FrameBuffer out) {
        //renderManager.getRenderer().setFrameBuffer(null);
    }

    public void cleanup() {
    }

    @Override
    public void simpleUpdate(float tpf){
//        angle += tpf * 0.25f;
//        angle %= FastMath.TWO_PI;
//
//        pl.setPosition(new Vector3f(FastMath.cos(angle) * 4f, 0.5f, FastMath.sin(angle) * 4f));
//        lightMdl.setLocalTranslation(pl.getPosition());
    }

}
