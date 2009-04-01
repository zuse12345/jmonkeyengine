package com.g3d.test;

import com.g3d.light.DirectionalLight;
import com.g3d.light.PointLight;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Node;
import com.g3d.scene.SceneManager;
import com.g3d.scene.Spatial;
import com.g3d.scene.material.ColorMaterial;
import com.g3d.scene.material.HeightMaterial;
import com.g3d.scene.material.NormalMaterial;
import com.g3d.scene.material.PhongMaterial;
import com.g3d.app.Application;
import com.g3d.app.SimpleApplication;
import com.g3d.system.DisplaySettings;
import com.g3d.terrain.BufferGeomap;
import com.g3d.terrain.Geomap;
import com.g3d.terrain.GeomapLoader;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads terrain from a heightmap image and then lights it with
 * phong shading.
 */
public class TestTerrainLighting extends SimpleApplication {

    private Geometry terrain;
    private float time = 0;

    public static void main(String[] args){
        TestTerrainLighting app = new TestTerrainLighting();
        app.start();
    }

    public void simpleInitApp() {
        Mesh terrainMesh = null;
        try {
            // create Geomap from image
            URL imageUrl = TestTerrainLighting.class.getResource("/com/g3d/test/data/heightmap.png");
            Geomap heightmap = GeomapLoader.fromImage(imageUrl);

            // generate mesh from the geomap
            terrainMesh = heightmap.createMesh(new Vector3f(1f, 0.005f, 1f), Vector2f.UNIT_XY, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // put the mesh in the geometry
        terrain = new Geometry("Terrain", terrainMesh);

        // use phong shading material
        terrain.setMaterial(new PhongMaterial(16f));
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

        // center the terrain
        Vector3f center = terrain.getModelBound().getCenter().clone();

        Camera camera = getCamera();
        Vector3f camPos = new Vector3f(0f,400,300);
        camera.setLocation(camPos);
        camera.lookAt(center, Vector3f.UNIT_Y);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // rotate the terrain
        time += tpf;
        time %= FastMath.TWO_PI;
        rootNode.setLocalRotation(new Quaternion(new float[]{ 0, time, 0}));
    }

}
