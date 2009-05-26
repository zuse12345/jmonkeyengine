package g3dtest.gui;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.scene.Geometry;
import com.g3d.scene.Quad;
import com.g3d.system.DisplaySettings;
import com.g3d.texture.Texture;

public class TestOrtho extends SimpleApplication {
    
    public static void main(String[] args){
        TestOrtho app = new TestOrtho();
        app.setSettings(new DisplaySettings(DisplaySettings.Template.Default1280x720));
        app.start();
    }

    public void simpleInitApp() {
        // create the mesh
        Quad q = new Quad(2, 2, true);

        // put mesh in geometry scene graph element
        Geometry g = new Geometry("Picture", q);

        // create material which extends definition "sprite2d.j3md"
        Material m = new Material(manager, "sprite2d.j3md");

        // load texture named "Monkey.dds"
        Texture t = manager.loadTexture("Monkey.dds");

        // set a parameter called "m_Texture" on the material to
        // the loaded texture
        m.setTexture("m_Texture", t);

        // set the geometry to use the material
        g.setMaterial(m);

        // attach geometry to root node
        rootNode.attachChild(g);
    }
}
