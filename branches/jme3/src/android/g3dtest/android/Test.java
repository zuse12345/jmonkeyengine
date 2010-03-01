package g3dtest.android;

import com.g3d.app.SimpleApplication;
import com.g3d.asset.TextureKey;
import com.g3d.material.Material;
import com.g3d.math.Transform;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Sphere;
import com.g3d.texture.Texture;
import g3dtools.converters.model.ModelConverter;

public class Test extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        Sphere s = new Sphere(8, 8, .5f);
        Geometry geom = new Geometry("sphere", s);
        ModelConverter.optimize(geom);

        Material mat = new Material(manager, "plain_texture.j3md");
        Texture tex = manager.loadTexture(new TextureKey("monkey.j3i"));
        mat.setTexture("m_ColorMap", tex);
//        geom.setMaterial(mat);

        for (int y = -1; y < 2; y++){
            for (int x = -1; x < 2; x++){
                Geometry geomClone = new Geometry("geom", s);
                geomClone.setMaterial(mat);
                geomClone.setLocalTranslation(x, y, 0);
                
                Transform t = geom.getTransform().clone();
                Transform t2 = geomClone.getTransform().clone();
                t.combineWithParent(t2);
                geomClone.setTransform(t);

                rootNode.attachChild(geomClone);
            }
        }
    }

}
