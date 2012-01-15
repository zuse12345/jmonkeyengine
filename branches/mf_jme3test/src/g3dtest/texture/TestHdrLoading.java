package g3dtest.texture;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.TextureKey;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Quad;
import com.g3d.system.AppSettings;
import com.g3d.texture.Image;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;

public class TestHdrLoading extends SimpleApplication {

    private Quad quadMesh;

    public static void main(String[] args){
        TestHdrLoading app = new TestHdrLoading();
        app.start();
    }

    public void createToneMappedPanel(Vector3f pos, Image img, float exposure){
        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.setLocalTranslation(pos);
//        Image tmImg = ToneMapper.toneMap(img, exposure);

        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", new Texture2D(img));
        quad.setMaterial(mat);

        float aspect = img.getWidth() / (float) img.getHeight();
        quad.setLocalScale(new Vector3f(5, 5 / aspect, 1));

        rootNode.attachChild(quad);
    }

    @Override
    public void simpleInitApp() {
        quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, true);
        quadMesh.updateBound();

        Texture original = manager.loadTexture("nave.hdr", true, true, false, 0);
//        createToneMappedPanel(new Vector3f(-5,0,0), original, 0.3f);
        createToneMappedPanel(new Vector3f(0,0,0),  original.getImage(), 1.5f);
//        createToneMappedPanel(new Vector3f(5,0,0),  original, 4f);
    }

}