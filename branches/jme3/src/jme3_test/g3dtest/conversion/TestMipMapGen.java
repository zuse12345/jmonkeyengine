package g3dtest.conversion;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Quad;
import com.g3d.texture.Image;
import com.g3d.texture.Texture;
import g3dtools.converters.MipMapGenerator;

public class TestMipMapGen extends SimpleApplication {

    public static void main(String[] args){
        TestMipMapGen app = new TestMipMapGen();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // create a simple plane/quad
        Quad quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, false);
        quadMesh.updateBound();

        Geometry quad1 = new Geometry("Textured Quad", quadMesh);
        Geometry quad2 = new Geometry("Textured Quad 2", quadMesh);

        Texture tex = manager.loadTexture("Monkey.png");
        tex.setMinFilter(Texture.MinFilter.Trilinear);

        Texture texCustomMip = tex.clone();
        Image imageCustomMip = texCustomMip.getImage().clone();
        MipMapGenerator.generateMipMaps(imageCustomMip);
        texCustomMip.setImage(imageCustomMip);

        Material mat1 = new Material(manager, "plain_texture.j3md");
        mat1.setTexture("m_ColorMap", tex);

        Material mat2 = new Material(manager, "plain_texture.j3md");
        mat2.setTexture("m_ColorMap", texCustomMip);

        quad1.setMaterial(mat1);
//        quad1.setLocalTranslation(1, 0, 0);

        quad2.setMaterial(mat2);
        quad2.setLocalTranslation(1, 0, 0);

        rootNode.attachChild(quad1);
        rootNode.attachChild(quad2);
    }

}
