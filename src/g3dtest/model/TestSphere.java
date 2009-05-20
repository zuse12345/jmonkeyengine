package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.light.DirectionalLight;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Quad;
import com.g3d.scene.Sphere;
import com.g3d.system.DisplaySettings;
import com.g3d.texture.Texture;

public class TestSphere extends SimpleApplication  {

    public static void main(String[] args){
        TestSphere app = new TestSphere();
        app.setSettings(new DisplaySettings(DisplaySettings.Template.Default640x480));
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere sphMesh = new Sphere(20, 20, 1);
        Material solidColor = manager.loadMaterial("red_color.j3m");
        
        for (int y = 0; y < 10; y++){
            for (int x = 0; x < 10; x++){
                Geometry sphere = new Geometry("sphere", sphMesh);

                sphere.setMaterial(solidColor);
                sphere.setLocalTranslation(x * 2, 0, y * 2);
                rootNode.attachChild(sphere);
            }
        }
        cam.setLocation(new Vector3f(0, 5, 0));
    }

}
