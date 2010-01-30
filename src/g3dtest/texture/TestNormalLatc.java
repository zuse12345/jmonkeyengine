package g3dtest.texture;

import com.g3d.app.SimpleApplication;
import com.g3d.font.BitmapFont;
import com.g3d.font.BitmapText;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Quad;
import com.g3d.texture.Texture;

public class TestNormalLatc extends SimpleApplication {

    private Quad quadMesh;

    public static void main(String[] args){
        TestNormalLatc app = new TestNormalLatc();
        app.start();
    }

    public Geometry createQuad(float side, String texName, boolean latc){
        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.updateModelBound();

        Texture tex = manager.loadTexture(texName, true, false, false, 16);
        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", tex);
//        mat.setBoolean("m_Normalize", true);
        if (latc)
            mat.setBoolean("m_LATC", true);

        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect * 5, 5, 1));
        quad.center();
        quad.setLocalTranslation(quad.getLocalTranslation().x + quad.getLocalScale().x  * side, 0, 0);

        return quad;
    }

    public void simpleInitApp() {
        BitmapFont font = manager.loadFont("cooper.fnt");
        BitmapText txt = new BitmapText(font, false);
        txt.setSize(font.getCharSet().getRenderedSize());
        txt.setText("Left: LATC, Middle: JPG, Right: DXT1nm");
        txt.setLocalTranslation(0, txt.getLineHeight() * 2, 0);
        guiNode.attachChild(txt);

        // create a simple plane/quad
        quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, false);

        rootNode.attachChild(createQuad(-1f, "dot3_latc.dds", true));
        rootNode.attachChild(createQuad(0f, "dot3.jpg", false));
        rootNode.attachChild(createQuad(1f, "dot3_dxt1.dds", false));
    }

}
