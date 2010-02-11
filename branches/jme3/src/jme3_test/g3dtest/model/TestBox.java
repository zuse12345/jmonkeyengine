package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.TextureKey;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Box;
import com.g3d.texture.Texture;

public class TestBox extends SimpleApplication {

    public static void main(String[] args){
        TestBox app = new TestBox();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        geom.updateModelBound();

        Material mat = new Material(manager, "plain_texture.j3md");
        TextureKey key = new TextureKey("Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        geom.setMaterial(mat);
        
        rootNode.attachChild(geom);
    }

}
