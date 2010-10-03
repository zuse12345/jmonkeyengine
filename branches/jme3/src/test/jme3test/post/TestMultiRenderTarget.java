package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.TangentBinormalGenerator;

public class TestMultiRenderTarget extends SimpleApplication implements SceneProcessor {

    private FrameBuffer fb;
    private Texture2D t1, t2, t3, t4;
    private Geometry sphere;
    private Picture display;

    public static void main(String[] args){
        TestMultiRenderTarget app = new TestMultiRenderTarget();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.addProcessor(this);

        Sphere sphMesh = new Sphere(32, 32, 1);
        sphMesh.setTextureMode(Sphere.TextureMode.Projected);
        sphMesh.updateGeometry(32, 32, 1, false, false);
        TangentBinormalGenerator.generate(sphMesh);

        sphere = new Geometry("Rock Ball", sphMesh);
        Material mat = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.j3m");
        mat.selectTechnique("GBuf");
        sphere.setMaterial(mat);
        rootNode.attachChild(sphere);

        PointLight pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        pl.setPosition(new Vector3f(0f, 0f, 4f));
        rootNode.addLight(pl);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1,-1,1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.22f, 0.15f, 0.1f, 1.0f));
        rootNode.addLight(dl);

        display = new Picture("Picture");
        display.move(0, 0, -1); // make it appear behind stats view
        display.setPosition(0, 0);
        display.setWidth(settings.getWidth());
        display.setHeight(settings.getHeight());
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
        viewPort.setOutputFrameBuffer(fb);
        guiViewPort.setClearEnabled(true);
    }

    public void reshape(ViewPort vp, int w, int h) {
        t1 = new Texture2D(w, h, Format.RGB8);
        display.setTexture(assetManager, t1, false);
        
        fb = new FrameBuffer(w, h, 0);
        fb.setDepthBuffer(Format.Depth);
        fb.setColorTexture(t1);
    }

    public boolean isInitialized() {
        return true;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
    }

    public void postFrame(FrameBuffer out) {
        //renderManager.getRenderer().setFrameBuffer(null);
    }

    public void cleanup() {
    }

}
