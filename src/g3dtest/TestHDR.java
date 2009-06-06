package g3dtest;

import com.g3d.app.SimpleApplication;
import com.g3d.light.DirectionalLight;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial;
import com.g3d.scene.Sphere;
import com.g3d.system.AppSettings;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture.MagFilter;
import com.g3d.texture.Texture.MinFilter;
import com.g3d.texture.Texture2D;
import com.g3d.ui.Picture;
import com.g3d.util.TangentBinormalGenerator;

public class TestHDR extends SimpleApplication {

    private Node fbNode = new Node("Framebuffer Node");
    private FrameBuffer fb = new FrameBuffer(800, 600, 0);
//    private Camera fbCam = new Camera(800, 600);

    public static void main(String[] args){
        TestHDR app = new TestHDR();
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
        Sphere sphMesh = new Sphere(32, 32, 1);
        sphMesh.setTextureMode(Sphere.TextureMode.Projected);
        sphMesh.updateGeometry(32, 32, 1, false);
        TangentBinormalGenerator.generate(sphMesh);
        Geometry sphere = new Geometry("Rock Ball", sphMesh);
        Material mat = manager.loadMaterial("pond_rock.j3m");
        sphere.setMaterial(mat);
        fbNode.attachChild(sphere);

        PointLight pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        pl.setPosition(new Vector3f(0f, 0f, -4f));
        fbNode.addLight(pl);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1,-1,1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.22f, 0.15f, 0.1f, 1.0f));
        fbNode.addLight(dl);

        //setup framebuffer's cam
//        fbCam.setFrustumPerspective(45f, (float)800 / 600, 1f, 1000f);
//        fbCam.setLocation(new Vector3f(0f, 0f, -3f));
//        fbCam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup main scene
        Picture p = new Picture("Picture");
        p.setPosition(0, 0);
        p.setWidth(800);
        p.setHeight(600);

        Material mat2 = new Material(manager, "sprite2d.j3md");
        mat2.setTexture("m_Texture", fbTex);
        p.setMaterial(mat2);
        
        rootNode.attachChild(p);
    }

    @Override
    public void simpleUpdate(float tpf){
        fbNode.updateGeometricState(tpf, true);
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
        r.setCamera(cam);
    }

}
