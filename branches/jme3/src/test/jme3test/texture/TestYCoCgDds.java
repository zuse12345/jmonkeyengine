package jme3test.texture;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

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
        // create a simple plane/quad
        quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, false);

        rootNode.attachChild(createQuad(-1f, "nightsky.png", false));
        rootNode.attachChild(createQuad(0,   "nightsky_dxt1.dds", false));
        rootNode.attachChild(createQuad(1f,  "nightsky_ycc.dds", true));
    }

}
