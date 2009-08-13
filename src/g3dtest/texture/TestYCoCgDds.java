package g3dtest.texture;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Quad;
import com.g3d.texture.Texture;

/**
 * Compares RGB8, DXT5-YCoCg and DXT1 with a skybox texture
 * @author Kirill
 */
public class TestYCoCgDds extends SimpleApplication {

    private Quad quadMesh;

    public static void main(String[] args){
        TestYCoCgDds app = new TestYCoCgDds();
        app.start();
    }

    public Geometry createQuad(float side, String texName, boolean ycocg){
        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.updateModelBound();

        Texture tex = manager.loadTexture(texName);
        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", tex);
        if (ycocg)
            mat.setBoolean("m_YCoCg", true);

        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect * 5, 5, 1));
        quad.center();
        quad.setLocalTranslation(quad.getLocalTranslation().x + quad.getLocalScale().x  * side, 0, 0);

        return quad;
    }

    public void simpleInitApp() {
        manager.setProperty("FlipImages", "true");

        // create a simple plane/quad
        quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, false);

        rootNode.attachChild(createQuad(-1f, "TestRGB.tga", false));
        rootNode.attachChild(createQuad(0f, "TestYCoCgDXT5.dds", true));
        rootNode.attachChild(createQuad(1f, "TestDXT1.dds", false));
    }

}
