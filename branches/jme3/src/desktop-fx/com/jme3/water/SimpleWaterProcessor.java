/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.water;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.shader.DefineList;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.shader.VarType;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 *
 * @author normenhansen
 */
public class SimpleWaterProcessor implements SceneProcessor {

    RenderManager rm;
    ViewPort vp;
    Spatial reflectionScene;
    ViewPort reflectionView;
    ViewPort refractionView;
    FrameBuffer reflectionBuffer;
    FrameBuffer refractionBuffer;
    Camera reflectionCam;
    Camera refractionCam;
    Texture2D reflectionTexture;
    Texture2D refractionTexture;
    Texture2D depthTexture;
    Texture2D normalTexture;
    Texture2D dudvTexture;
    protected int textureWidth = 512;
    protected int textureHeight = 512;
    protected Plane plane = new Plane(Vector3f.UNIT_Y, Vector3f.ZERO.dot(Vector3f.UNIT_Y));
    Ray ray = new Ray();
    Vector3f targetLocation = new Vector3f();
    AssetManager manager;
    Material material;
    protected boolean debug = false;
    private Picture dispRefraction;
    private Picture dispReflection;
    private Picture dispDepth;

    public SimpleWaterProcessor(AssetManager manager) {
        this.manager = manager;
        material = new Material(manager, "Common/MatDefs/Water/SimpleWater.j3md");
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        this.rm = rm;
        this.vp = vp;

        loadTextures(manager);
        createTextures();
        applyTextures(material);

        createPreViews();

        dispRefraction = new Picture("dispRefraction");
        dispRefraction.setTexture(manager, refractionTexture, false);
        dispReflection = new Picture("dispRefraction");
        dispReflection.setTexture(manager, reflectionTexture, false);
        dispDepth = new Picture("depthTexture");
        dispDepth.setTexture(manager, depthTexture, false);
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return rm != null;
    }
    float time = 0;

    public void preFrame(float tpf) {
        time = time + tpf / 20;
        if (time > 1f) {
            time = 0;
        }
        material.setFloat("m_time", time);
        material.setFloat("m_time2", -time);
    }

    public void postQueue(RenderQueue rq) {
        Camera sceneCam = rm.getCurrentCamera();
        //update ray
        ray.setOrigin(sceneCam.getLocation());
        ray.setDirection(sceneCam.getDirection());

        //update refraction cam
        refractionCam.setLocation(sceneCam.getLocation());
        refractionCam.setRotation(sceneCam.getRotation());
        refractionCam.setFrustum(sceneCam.getFrustumNear(),
                sceneCam.getFrustumFar(),
                sceneCam.getFrustumLeft(),
                sceneCam.getFrustumRight(),
                sceneCam.getFrustumTop(),
                sceneCam.getFrustumBottom());

        //update reflection cam
        boolean inv=false;
        if(!ray.intersectsWherePlane(plane, targetLocation)){
            ray.setDirection(ray.getDirection().negateLocal());
            ray.intersectsWherePlane(plane, targetLocation);
            inv=true;
        }
        reflectionCam.setLocation(plane.reflect(sceneCam.getLocation(), new Vector3f()));
        reflectionCam.setFrustum(sceneCam.getFrustumNear(),
                sceneCam.getFrustumFar(),
                sceneCam.getFrustumLeft(),
                sceneCam.getFrustumRight(),
                sceneCam.getFrustumTop(),
                sceneCam.getFrustumBottom());
        reflectionCam.lookAt(targetLocation, Vector3f.UNIT_Y);
        if(inv){
            reflectionCam.setAxes(reflectionCam.getLeft().negateLocal(),reflectionCam.getUp(),reflectionCam.getDirection().negateLocal());
        }
    }

    public void postFrame(FrameBuffer out) {
        if (debug) {
            displayMap(rm.getRenderer(), dispRefraction, 64);
            displayMap(rm.getRenderer(), dispReflection, 256);
            displayMap(rm.getRenderer(), dispDepth, 448);
        }
    }

    public void cleanup() {
    }

    //debug only : displays maps
    protected void displayMap(Renderer r, Picture pic, int left) {
        Camera cam = vp.getCamera();
        rm.setCamera(cam, true);
        int h = cam.getHeight();

        pic.setPosition(left, h / 20f);

        pic.setWidth(128);
        pic.setHeight(128);
        pic.updateGeometricState();
        rm.renderGeometry(pic);
        rm.setCamera(cam, false);
    }

    public void setReflectionScene(Spatial spat) {
        reflectionScene = spat;
    }

    /**
     * Get the water material from this processor, apply this to your water quad.
     * @return
     */
    public Material getMaterial() {
        return material;
    }

    protected void loadTextures(AssetManager manager) {
        normalTexture = (Texture2D) manager.loadTexture("Textures/Water/gradient_map.jpg");
        dudvTexture = (Texture2D) manager.loadTexture("Textures/Water/dudv_map.jpg");
        normalTexture.setWrap(WrapMode.Repeat);
        dudvTexture.setWrap(WrapMode.Repeat);
    }

    protected void createTextures() {
        reflectionTexture = new Texture2D(textureWidth, textureHeight, Format.RGB8);
        refractionTexture = new Texture2D(textureWidth, textureHeight, Format.RGB8);
        depthTexture = new Texture2D(textureWidth, textureHeight, Format.Depth);
    }

    protected void applyTextures(Material mat) {
        mat.setTexture("m_water_reflection", reflectionTexture);
        mat.setTexture("m_water_refraction", refractionTexture);
        mat.setTexture("m_water_depthmap", depthTexture);
        mat.setTexture("m_water_normalmap", normalTexture);
        mat.setTexture("m_water_dudvmap", dudvTexture);
    }

    protected void createPreViews() {
        reflectionCam = new Camera(textureWidth, textureHeight);
        refractionCam = new Camera(textureWidth, textureHeight);

        // create a pre-view. a view that is rendered before the main view
        reflectionView = rm.createPreView("Reflection View", reflectionCam);
        reflectionView.setClearEnabled(true);
        reflectionView.setBackgroundColor(ColorRGBA.Black);
        // create offscreen framebuffer
        reflectionBuffer = new FrameBuffer(textureWidth, textureHeight, 0);
        //setup framebuffer to use texture
        reflectionBuffer.setDepthBuffer(Format.Depth);
        reflectionBuffer.setColorTexture(reflectionTexture);

        //set viewport to render to offscreen framebuffer
        reflectionView.setOutputFrameBuffer(reflectionBuffer);
        reflectionView.addProcessor(new ReflectionProcessor(manager));
        // attach the scene to the viewport to be rendered
        reflectionView.attachScene(reflectionScene);

        // create a pre-view. a view that is rendered before the main view
        refractionView = rm.createPreView("Refraction View", refractionCam);
        refractionView.setClearEnabled(true);
        refractionView.setBackgroundColor(ColorRGBA.Black);
        // create offscreen framebuffer
        refractionBuffer = new FrameBuffer(textureWidth, textureHeight, 0);
        //setup framebuffer to use texture
        refractionBuffer.setDepthBuffer(Format.Depth);
        refractionBuffer.setColorTexture(refractionTexture);
        refractionBuffer.setDepthTexture(depthTexture);
        //set viewport to render to offscreen framebuffer
        refractionView.setOutputFrameBuffer(refractionBuffer);
        refractionView.addProcessor(new RefractionProcessor());
        // attach the scene to the viewport to be rendered
        refractionView.attachScene(reflectionScene);
    }

    protected void destroyViews() {
        rm.removePreView(reflectionView);
        rm.removePreView(refractionView);
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    /**
     * Set the reflection Texture width,
     * set before adding the processor!
     * @param textureWidth
     */
    public void setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    /**
     * Set the reflection Texture height,
     * set before adding the processor!
     * @param textureWidth
     */
    public void setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
    }

    public Plane getPlane() {
        return plane;
    }

    /**
     * Set the water plane for this processor,
     * set before adding the processor!
     * @param plane
     */
    public void setPlane(Plane plane) {
        this.plane = plane;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Reflection Processor
     */
    public class ReflectionProcessor implements SceneProcessor {

        RenderManager rm;
        ViewPort vp;
        Shader clipShader;
        AssetManager manager;

        public ReflectionProcessor(AssetManager manager) {
            this.manager = manager;
            DefineList defList = new DefineList();
            Quaternion vec = new Quaternion(plane.getNormal().x, plane.getNormal().y, plane.getNormal().z, plane.getConstant());
            ShaderKey key = new ShaderKey("Common/MatDefs/Water/clip_plane.vert", "Common/MatDefs/Water/clip_plane.frag", defList, "GLSL100");
            clipShader = manager.loadShader(key);
            clipShader.getUniform("u_clipPlane").setValue(VarType.Vector4, vec);
        }

        public void initialize(RenderManager rm, ViewPort vp) {
            this.rm = rm;
            this.vp = vp;
        }

        public void reshape(ViewPort vp, int w, int h) {
        }

        public boolean isInitialized() {
            return rm != null;
        }

        public void preFrame(float tpf) {
        }

        public void postQueue(RenderQueue rq) {
            rm.getRenderer().setShader(clipShader);
        }

        public void postFrame(FrameBuffer out) {
        }

        public void cleanup() {
            rm.getRenderer().deleteShader(clipShader);
        }
    }

    /**
     * Refraction Processor
     */
    public class RefractionProcessor implements SceneProcessor {

        RenderManager rm;
        ViewPort vp;

        public void initialize(RenderManager rm, ViewPort vp) {
            this.rm = rm;
            this.vp = vp;
        }

        public void reshape(ViewPort vp, int w, int h) {
        }

        public boolean isInitialized() {
            return rm != null;
        }

        public void preFrame(float tpf) {
        }

        public void postQueue(RenderQueue rq) {
//        rm.getRenderer().setClipPlane(0, -1, 0, 0);
        }

        public void postFrame(FrameBuffer out) {
//        rm.getRenderer().clearClipPlane();
        }

        public void cleanup() {
//        rm.getRenderer().clearClipPlane();
        }
    }
}
