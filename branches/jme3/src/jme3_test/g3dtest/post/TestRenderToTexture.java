package g3dtest.post;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.ViewPort;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Box;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;

/**
 * This test renders a scene to a texture, then displays the texture on a cube.
 */
public class TestRenderToTexture extends SimpleApplication {

    private Geometry offBox;
    private float angle = 0;

    public static void main(String[] args){
        TestRenderToTexture app = new TestRenderToTexture();
        app.start();
    }

    public Texture setupOffscreenView(){
        Camera offCamera = new Camera(512, 512);

        // create a pre-view. a view that is rendered before the main view
        ViewPort offView = renderManager.createPreView("Offscreen View", offCamera);
        offView.setBackgroundColor(ColorRGBA.DarkGray);

        // create offscreen framebuffer
        FrameBuffer offBuffer = new FrameBuffer(512, 512, 0);

        //setup framebuffer's cam
        offCamera.setFrustumPerspective(45f, 1f, 1f, 1000f);
        offCamera.setLocation(new Vector3f(0f, 0f, -5f));
        offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup framebuffer's texture
        Texture2D offTex = new Texture2D(512, 512, Format.RGB8);
        offTex.setAnisotropicFilter(4);

        //setup framebuffer to use texture
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorTexture(offTex);
        
        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        // setup framebuffer's scene
        Box boxMesh = new Box(Vector3f.ZERO, 1,1,1);
        Material material = (Material) manager.loadContent("jme_logo.j3m");
        offBox = new Geometry("sphere", boxMesh);
        offBox.setMaterial(material);

        // attach the scene to the viewport to be rendered
        offView.attachScene(offBox);
        
        return offTex;
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(3, 3, 3));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        //setup main scene
        Geometry quad = new Geometry("box", new Box(Vector3f.ZERO, 1,1,1));

        Texture offTex = setupOffscreenView();

        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", offTex);
        quad.setMaterial(mat);
        rootNode.attachChild(quad);
    }

    @Override
    public void simpleUpdate(float tpf){
        Quaternion q = new Quaternion();
        angle += tpf;
        angle %= FastMath.TWO_PI;
        q.fromAngles(angle, 0, angle);

        offBox.setLocalRotation(q);
        offBox.updateLogicalState(tpf);
        offBox.updateGeometricState();
    }


}
