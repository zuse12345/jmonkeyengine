package g3dtest.texture;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.res.plugins.HDRLoader;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.Quad;
import com.g3d.system.DisplaySettings;
import com.g3d.texture.Image;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import com.g3d.util.ToneMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

public class TestHdrLoading extends SimpleApplication {

    private Quad quadMesh;

    public static void main(String[] args){
        TestHdrLoading app = new TestHdrLoading();
        app.setSettings(new DisplaySettings(DisplaySettings.Template.Default640x480));
        app.start();
    }

    public void createToneMappedPanel(Vector3f pos, Image img, float exposure){
        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.setLocalTranslation(pos);
        Image tmImg = ToneMapper.toneMap(img, exposure);

        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", new Texture2D(tmImg));
        quad.setMaterial(mat);
//        quad.setMaterial(new TextureMaterial(new Texture2D(tmImg), true));

        float aspect = img.getWidth() / (float) img.getHeight();
        quad.setLocalScale(new Vector3f(5, 5 / aspect, 1));

        rootNode.attachChild(quad);
    }

    @Override
    public void simpleInitApp() {
        quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, true, true);
        quadMesh.updateBound();

        Image original = manager.loadImage("nave.hdr");
        createToneMappedPanel(new Vector3f(-5,0,0), original, 0.3f);
        createToneMappedPanel(new Vector3f(0,0,0),  original, 1.5f);
        createToneMappedPanel(new Vector3f(5,0,0),  original, 4f);
    }

}
