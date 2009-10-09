package g3dtest.post;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.shape.Sphere;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture2D;
import com.g3d.ui.Picture;

/**
 * Demonstrates FrameBuffer usage.
 * The scene is first rendered to an FB with a texture attached,
 * the texture is then rendered onto the screen in ortho mode.
 *
 * @author Kirill
 */
public class TestFBOPassthrough extends SimpleApplication {

    private Node fbNode = new Node("Framebuffer Node");
    private FrameBuffer fb;

    public static void main(String[] args){
        TestFBOPassthrough app = new TestFBOPassthrough();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        int w = settings.getWidth();
        int h = settings.getHeight();

        //setup framebuffer
        fb = new FrameBuffer(w, h, 0);

        Texture2D fbTex = new Texture2D(w, h, Format.RGB8);
        fb.setDepthBuffer(Format.Depth);
        fb.setColorTexture(fbTex);

        // setup framebuffer's scene
        Sphere sphMesh = new Sphere(20, 20, 1);
        Material solidColor = manager.loadMaterial("red_color.j3m");

        Geometry sphere = new Geometry("sphere", sphMesh);
        sphere.setMaterial(solidColor);
        fbNode.attachChild(sphere);

        //setup main scene
        Picture p = new Picture("Picture");
        p.setPosition(0, 0);
        p.setWidth(w);
        p.setHeight(h);
        p.setTexture(manager, fbTex, false);

        rootNode.attachChild(p);
    }

    @Override
    public void simpleUpdate(float tpf){
        fbNode.updateLogicalState(tpf);
        fbNode.updateGeometricState();
    }

    @Override
    public void simpleRender(Renderer r){
        //do FBO rendering
        r.setFrameBuffer(fb);

        r.setCamera(cam); // FBO uses current camera
        r.clearBuffers(true, true, true);
        render(fbNode, r);
        r.renderQueue();

        //go back to default rendering and let
        //SimpleApplication render the default scene
        r.setFrameBuffer(null);
    }

}
