package com.g3d.shadow;

import com.g3d.material.Material;
import com.g3d.math.Matrix4f;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue.ShadowMode;
import com.g3d.renderer.queue.SpatialList;
import com.g3d.res.ContentManager;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture2D;
import com.g3d.ui.Picture;

public class BasicShadowRenderer {

    private FrameBuffer shadowFB;
    private Texture2D shadowMap;
    private Camera shadowCam;

    private Material preshadowMat;
    private Material postshadowMat;
    private Material dispMat;
    private Picture dispPic = new Picture("Display");

    private Vector3f[] points = new Vector3f[8];

    public BasicShadowRenderer(ContentManager manager, int size){
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

    public Vector3f[] getPoints() {
        return points;
    }

    public void postQueue(Renderer r){
        // update frustum points based on current camera
        Camera viewCam = r.getCamera();
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
        SpatialList occluders = r.getRenderQueue().getShadowQueueContent(ShadowMode.Cast);
        SpatialList recievers = r.getRenderQueue().getShadowQueueContent(ShadowMode.Recieve);
        ShadowUtil.updateShadowCamera(occluders, recievers, shadowCam, points);

        r.setFrameBuffer(shadowFB);
        r.setCamera(shadowCam);
        r.clearBuffers(false,true,false);
        r.setForcedMaterial(preshadowMat);

        r.renderShadowQueue(ShadowMode.Cast);

        r.setForcedMaterial(null);
        r.setCamera(viewCam);
        r.setFrameBuffer(null);
    }

    public Camera getShadowCamera(){
        return shadowCam;
    }

    public void displayShadowMap(Renderer r){
        Camera cam = r.getCamera();
        int w = cam.getWidth();
        int h = cam.getHeight();
        
        dispPic.setPosition(w / 20f, h / 20f);
        dispPic.setWidth(w / 5f);
        dispPic.setHeight(h / 5f);
        dispPic.setMaterial(dispMat);
        dispPic.updateGeometricState(0, true);
        r.renderGeometry(dispPic);
    }

    public void postRender(Renderer r){
        r.renderQueue();

        postshadowMat.setMatrix4("m_LightViewProjectionMatrix", shadowCam.getViewProjectionMatrix());
        r.setForcedMaterial(postshadowMat);
        r.renderShadowQueue(ShadowMode.Recieve);
        r.setForcedMaterial(null);

        displayShadowMap(r);
    }

}
