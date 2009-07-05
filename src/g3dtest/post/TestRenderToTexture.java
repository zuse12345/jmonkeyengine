package g3dtest.post;

import com.g3d.app.SimpleApplication;
import com.g3d.light.DirectionalLight;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.shape.Box;
import com.g3d.system.AppSettings;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture2D;
import com.g3d.util.TangentBinormalGenerator;

/**
 * This test renders a scene to a texture, then displays the texture on a cube.
 */
public class TestRenderToTexture extends SimpleApplication {

    private Node fbNode = new Node("Framebuffer Node");

    // uncomment to test multisampled FB
    private FrameBuffer msFb = null; //new FrameBuffer(512, 512, 4);
    private FrameBuffer fb = new FrameBuffer(512, 512, 0);
    private Camera fbCam = new Camera(512, 512);

    private Geometry fbBox;
    private float angle = 0;

    public static void main(String[] args){
        TestRenderToTexture app = new TestRenderToTexture();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        manager.setProperty("EnableMipmapGen", "true");
        manager.setProperty("TexAnisoLevel", "4"); //maximum

        cam.setLocation(new Vector3f(3, 3, 3));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        
        //setup framebuffer
        if (msFb != null){
           msFb.setDepthBuffer(Format.Depth);
           msFb.setColorBuffer(Format.RGB8);
        }

        Texture2D fbTex = new Texture2D(512, 512, Format.RGB8);
        fbTex.setAnisotropicFilter(4);
        fb.setDepthBuffer(Format.Depth);
        fb.setColorTexture(fbTex);

        // setup framebuffer's scene
        Box boxMesh = new Box(Vector3f.ZERO, 1,1,1);
        Material solidColor = manager.loadMaterial("jme_logo.j3m");
        fbBox = new Geometry("sphere", boxMesh);
        fbBox.updateModelBound();
        fbBox.setMaterial(solidColor);
        fbNode.attachChild(fbBox);

        //setup framebuffer's cam
        fbCam.setFrustumPerspective(45f, 1f, 1f, 1000f);
        fbCam.setLocation(new Vector3f(0f, 0f, -5f));
        fbCam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup main scene
        Geometry quad = new Geometry("box", new Box(Vector3f.ZERO, 1,1,1));
        TangentBinormalGenerator.generate(quad.getMesh());
        quad.updateModelBound();

        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", fbTex);
        quad.setMaterial(mat);
        rootNode.attachChild(quad);

        rootNode.updateGeometricState(0, true);
    }

    @Override
    public void simpleUpdate(float tpf){
        Quaternion q = new Quaternion();
        angle += tpf;
        angle %= FastMath.TWO_PI;
        q.fromAngles(angle, 0, angle);
        fbBox.setLocalRotation(q);

        fbNode.updateGeometricState(0, true);
    }

    @Override
    public void simpleRender(Renderer r){
        // make sure all previous objects have rendered
        r.renderQueue();

        //do FBO rendering
        if (msFb != null)
            r.setFrameBuffer(msFb);
        else
            r.setFrameBuffer(fb);

        r.setCamera(fbCam);
        r.setBackgroundColor(ColorRGBA.DarkGray);
        r.clearBuffers(true, true, true);
        render(fbNode, r);
        r.renderQueue();

        r.setBackgroundColor(ColorRGBA.Black);
        //go back to default rendering and let
        //SimpleApplication render the default scene
        r.setFrameBuffer(null);
        if (msFb != null)
            r.copyFrameBuffer(msFb, fb);
        
        r.setCamera(cam);
    }

}
