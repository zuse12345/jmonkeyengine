package g3dtest.model;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Sphere;
import com.g3d.system.AppSettings;

public class TestSphere extends SimpleApplication  {

    public static void main(String[] args){
        TestSphere app = new TestSphere();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere sphMesh = new Sphere(20, 20, 1);
        Material solidColor = manager.loadMaterial("red_color.j3m");

        for (int y = -5; y < 5; y++){
            for (int x = -5; x < 5; x++){
                Geometry sphere = new Geometry("sphere", sphMesh);
                sphere.updateModelBound();

                sphere.setMaterial(solidColor);
                sphere.setLocalTranslation(x * 2, 0, y * 2);
                rootNode.attachChild(sphere);
            }
        }
        cam.setLocation(new Vector3f(0, 5, 0));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

}
