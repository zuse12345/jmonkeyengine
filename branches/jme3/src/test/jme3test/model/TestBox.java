package jme3test.model;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

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
