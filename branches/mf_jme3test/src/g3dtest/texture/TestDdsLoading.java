package g3dtest.texture;

import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.scene.Quad;
import com.g3d.system.DisplaySettings;
import com.g3d.texture.Texture;

public class TestDdsLoading extends SimpleApplication {

    public static void main(String[] args){
        TestDdsLoading app = new TestDdsLoading();
        app.setSettings(new DisplaySettings(DisplaySettings.Template.Default640x480));
        app.start();
    }

    public void simpleInitApp() {
        // create a simple plane/quad
        Quad quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, true, true);

        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.updateModelBound();

        Texture tex = manager.loadTexture("Monkey.dds");
        
        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", tex);
        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect * 5, 5, 1));

        rootNode.attachChild(quad);
    }

}
