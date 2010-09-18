package jme3test.terrain;

import jme3tools.converters.ImageToAwt;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;

public class TerrainTest extends SimpleApplication {

	private TerrainQuad terrain;
	Material matRock;
	Material matWire;
	boolean wireframe = true;
	protected BitmapText hintText;
	PointLight pl;
	Geometry lightMdl;

	public static void main(String[] args) {
		TerrainTest app = new TerrainTest();
		app.start();
	}

	@Override
	public void initialize() {
		super.initialize();

		loadHintText();
	}

	@Override
	public void simpleInitApp() {
		setupKeys();

        // First, we load up our textures and the heightmap texture for the terrain

		// TERRAIN TEXTURE material
		matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

		// ALPHA map (for splat textures)
		matRock.setTexture("m_Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

		// HEIGHTMAP image (for the terrain heightmap)
		Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

		// GRASS texture
		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		matRock.setTexture("m_Tex1", grass);
		matRock.setFloat("m_Tex1Scale", 64f);

		// DIRT texture
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		matRock.setTexture("m_Tex2", dirt);
		matRock.setFloat("m_Tex2Scale", 32f);

		// ROCK texture
		Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		matRock.setTexture("m_Tex3", rock);
		matRock.setFloat("m_Tex3Scale", 128f);

		// WIREFRAME material
		matWire = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");


		// CREATE HEIGHTMAP
		AbstractHeightMap heightmap = null;
		try {
			//heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

			heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 1f);
			heightmap.load();

		} catch (Exception e) {
			e.printStackTrace();
		}

                /*
                 * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
                 * terrain will be 513x513. It uses the heightmap we created to generate the height values.
                 */
		/**
		 * Optimal terrain patch size is 65 (64x64)
		 * If you go for small patch size, it will definitely slow down because the depth of
		 * the quad tree will increase, and more is done on the CPU then to traverse it.
                 * I plan to give each node in the tree a reference to its neighbours so that should
                 * resolve any of these slowdowns. -Brent
		 *
		 * The total size is up to you. At 1025 it ran fine for me (200+FPS), however at
		 * size=2049, it got really slow. But that is a jump from 2 million to 8 million triangles...
		 */
		terrain = new TerrainQuad("terrain", 65, 513, new Vector3f(1, 1, 1), heightmap.getHeightMap());
		List<Camera> cameras = new ArrayList<Camera>();
		cameras.add(getCamera());
		TerrainLodControl control = new TerrainLodControl(terrain, cameras);
		terrain.addControl(control);
		terrain.setMaterial(matRock);
		terrain.setModelBound(new BoundingBox());
		terrain.updateModelBound();
		terrain.setLocalTranslation(0, -100, 0);
		terrain.setLocalScale(2f, 1f, 2f);
		rootNode.attachChild(terrain);

//		lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
//		lightMdl.setMaterial((Material) assetManager.loadAsset(new AssetKey("Common/Materials/RedColor.j3m")));
//		rootNode.attachChild(lightMdl);

		// flourescent main light
//		pl = new PointLight();
//		pl.setColor(new ColorRGBA(0.88f, 0.92f, 0.95f, 1.0f));
//		pl.setPosition(new Vector3f(0, 0, 15));
//		rootNode.addLight(pl);
//
//		DirectionalLight dl = new DirectionalLight();
//		dl.setDirection(new Vector3f(1, -0.5f, -0.1f).normalizeLocal());
//		dl.setColor(new ColorRGBA(0.50f, 0.40f, 0.50f, 1.0f));
//		rootNode.addLight(dl);


		getCamera().getLocation().y = 10;
		getCamera().setDirection(new Vector3f(0, -1.5f, -1));
	}

	public void loadHintText() {
		hintText = new BitmapText(guiFont, false);
		hintText.setSize(guiFont.getCharSet().getRenderedSize());
		hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
		hintText.setText("Hit T to switch to wireframe");
		guiNode.attachChild(hintText);
	}

	private void setupKeys() {
		flyCam.setMoveSpeed(50);
		inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addListener(actionListener, "wireframe");
	}

	private ActionListener actionListener = new ActionListener() {

		@Override
		public void onAction(String name, boolean pressed, float tpf) {
			if (name.equals("wireframe") && !pressed) {
				wireframe = !wireframe;
				if (!wireframe) {
						terrain.setMaterial(matWire);
				} else {
					terrain.setMaterial(matRock);
				}

			}
		}
	};
}
