package com.g3d.shadow;

import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue;
import com.g3d.renderer.queue.RenderQueue.ShadowMode;
import com.g3d.renderer.queue.GeometryList;
import com.g3d.asset.AssetManager;
import com.g3d.post.SceneProcessor;
import com.g3d.renderer.RenderManager;
import com.g3d.renderer.ViewPort;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture2D;
import com.g3d.ui.Picture;

public class BasicShadowRenderer implements SceneProcessor {

    private RenderManager renderManager;
    private ViewPort viewPort;

    private FrameBuffer shadowFB;
    private Texture2D shadowMap;
    private Camera shadowCam;

    private Material preshadowMat;
    private Material postshadowMat;
    private Material dispMat;
    private Picture dispPic = new Picture("Display");

    private Vector3f[] points = new Vector3f[8];

    public BasicShadowRenderer(AssetManager manager, int size){
        shadowFB =  new FrameBuffer(size,size,0);
        shadowMap = new Texture2D(size,size,Format.Depth);
        shadowFB.setDepthTexture(shadowMap);
        shadowCam = new Camera(size,size);
        
        preshadowMat  = new Material(manager, "preshadow.j3md");
        postshadowMat = new Material(manager, "postshadow.j3md");
        postshadowMat.setTexture("m_ShadowMap", shadowMap);

        dispMat = new Material(manager, "sprite2d.j3md");
        dispMat.setTexture("m_Texture", shadowMap);

        for (int i = 0; i < points.length; i++){
            points[i] = new Vector3f();
        }
    }

    public void initialize(RenderManager rm, ViewPort vp){
        renderManager = rm;
        viewPort = vp;
    }

    public Vector3f[] getPoints() {
        return points;
    }

    public void postQueue(RenderQueue rq){
        // update frustum points based on current camera
        Camera viewCam = viewPort.getCamera();
        ShadowUtil.updateFrustumPoints(viewCam,
                                       viewCam.getFrustumNear(),
                                       viewCam.getFrustumFar(), 
                                       1.0f,
                                       points);

        // update light direction
        shadowCam.setProjectionMatrix(null);
        shadowCam.setFrustumPerspective(45, 1, 1, 20);
        shadowCam.setLocation(new Vector3f(5, 5, 5));
        shadowCam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        shadowCam.update();
        shadowCam.updateViewProjection();

        // render shadow casters to shadow map
        GeometryList occluders = rq.getShadowQueueContent(ShadowMode.Cast);
        GeometryList recievers = rq.getShadowQueueContent(ShadowMode.Recieve);
        ShadowUtil.updateShadowCamera(occluders, recievers, shadowCam, points);

        Renderer r = renderManager.getRenderer();
        renderManager.setCamera(shadowCam);
        renderManager.setForcedMaterial(preshadowMat);

        r.setFrameBuffer(shadowFB);
        r.clearBuffers(false,true,false);
        viewPort.getQueue().renderShadowQueue(ShadowMode.Cast, renderManager, shadowCam);
        r.setFrameBuffer(null);

        renderManager.setForcedMaterial(null);
        renderManager.setCamera(viewCam);
    }

    public Camera getShadowCamera(){
        return shadowCam;
    }

    public void displayShadowMap(Renderer r){
        Camera cam = viewPort.getCamera();
        int w = cam.getWidth();
        int h = cam.getHeight();
        
        dispPic.setPosition(w / 20f, h / 20f);
        dispPic.setWidth(w / 5f);
        dispPic.setHeight(h / 5f);
        dispPic.setMaterial(dispMat);
        dispPic.updateGeometricState();
        renderManager.renderGeometry(dispPic);
    }

    public void postFrame(FrameBuffer out){
        postshadowMat.setMatrix4("m_LightViewProjectionMatrix", shadowCam.getViewProjectionMatrix());
        renderManager.setForcedMaterial(postshadowMat);
        viewPort.getQueue().renderShadowQueue(ShadowMode.Recieve, renderManager, viewPort.getCamera());
        renderManager.setForcedMaterial(null);

        displayShadowMap(renderManager.getRenderer());
    }

    public void preFrame(float tpf) {
    }

    public void cleanup() {
    }

}
