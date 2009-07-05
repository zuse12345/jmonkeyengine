package g3dtest;

import com.g3d.app.SimpleApplication;
import com.g3d.light.DirectionalLight;
import com.g3d.light.PointLight;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.shape.Sphere;
import com.g3d.terrain.Geomap;
import com.g3d.terrain.GeomapLoader;
import java.io.IOException;
import java.net.URL;

/**
 * Loads terrain from a heightmap image and then lights it with
 * phong shading.
 */
public class TestTerrainLighting extends SimpleApplication {

    private Geometry terrain;
//    private float time = 0;

    public static void main(String[] args){
        TestTerrainLighting app = new TestTerrainLighting();
        app.start();
    }

    public void simpleInitApp() {
        Mesh terrainMesh = null;
        try {
            // create Geomap from image
            URL imageUrl = TestTerrainLighting.class.getResource("/textures/heightmap.png");
            Geomap heightmap = GeomapLoader.fromImage(imageUrl);

            // generate mesh from the geomap
            //terrainMesh = heightmap.createMesh(new Vector3f(1f, 0.005f, 1f), Vector2f.UNIT_XY, true);
            //terrainMesh.setStatic();
            terrainMesh = new Sphere(32,32,1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // put the mesh in the geometry
        terrain = new Geometry("Terrain", terrainMesh);
        terrain.updateModelBound();

        // use phong shading material
        Material mat = new Material(manager, "phong_lighting.j3md");
//        Material mat = manager.loadMaterial("red_color.j3m");
        terrain.setMaterial(mat);
        rootNode.attachChild(terrain);

        // create point light at the center with radius 100
        PointLight pl = new PointLight();
        pl.setColor(ColorRGBA.Green);
        pl.setPosition(new Vector3f(0, 100, 0));
        pl.setRadius(100);
        rootNode.addLight(pl);

        // create directional light from the front
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(0f, -1f, -1f).normalizeLocal());
        dl.setColor(ColorRGBA.Red.multLocal(0.3f));
        rootNode.addLight(dl);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // rotate the terrain
//        time += tpf;
//        time %= FastMath.TWO_PI;
//        rootNode.setLocalRotation(new Quaternion(new float[]{ 0, time, 0}));
    }

}
