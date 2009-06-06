package g3dtest;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.Quad;
import com.g3d.scene.Sphere;
import com.g3d.system.AppSettings;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture.MagFilter;
import com.g3d.texture.Texture.MinFilter;
import com.g3d.texture.Texture2D;
import com.g3d.ui.Picture;

public class TestFBOPassthrough extends SimpleApplication {

    private Node fbNode = new Node("Framebuffer Node");
    private FrameBuffer fb = new FrameBuffer(800, 600, 0);
//    private Camera fbCam = new Camera(800, 600);

    public static void main(String[] args){
        TestFBOPassthrough app = new TestFBOPassthrough();
        app.setSettings(new AppSettings(AppSettings.Template.Default800x600));
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //setup framebuffer
        Texture2D fbTex = new Texture2D(800, 600, Format.RGB8);
        fbTex.setMagFilter(MagFilter.Nearest);
        fbTex.setMinFilter(MinFilter.NearestNoMipMaps);

        fb.setDepthBuffer(Format.Depth);
        fb.setColorTexture(fbTex);

        // setup framebuffer's scene
        Sphere sphMesh = new Sphere(20, 20, 1);
        Material solidColor = manager.loadMaterial("red_color.j3m");

        Geometry sphere = new Geometry("sphere", sphMesh);
        sphere.setMaterial(solidColor);
        fbNode.attachChild(sphere);

        //setup framebuffer's cam
//        fbCam.setFrustumPerspective(45f, (float)800 / 600, 1f, 1000f);
//        fbCam.setLocation(new Vector3f(0f, 0f, -3f));
//        fbCam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup main scene
        Picture p = new Picture("Picture");
        p.setPosition(0, 0);
        p.setWidth(800);
        p.setHeight(600);
        p.setTexture(manager, fbTex, false);

        rootNode.attachChild(p);
    }

    @Override
    public void simpleUpdate(float tpf){
        fbNode.updateGeometricState(0, true);
    }

    @Override
    public void simpleRender(Renderer r){
        //do FBO rendering
        r.setFrameBuffer(fb);
        r.setCamera(cam); // FBO uses current camera
        r.clearBuffers(true, true, true);
        render(fbNode, r);

        //go back to default rendering and let
        //SimpleApplication render the default scene
        r.setFrameBuffer(null);
//        r.setCamera(cam);
    }

}
