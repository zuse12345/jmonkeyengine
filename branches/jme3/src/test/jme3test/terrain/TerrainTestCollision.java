package jme3test.terrain;

import jme3tools.converters.ImageToAwt;
import com.jme3.app.SimpleBulletApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
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
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.jbullet.TerrainPhysicsShapeFactory;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a terrain object and a collision node to go with it. Then
 * drops several balls from the sky that collide with the terrain
 * and roll around.
 *
 * @author Brent Owens
 */
public class TerrainTestCollision extends SimpleBulletApplication {

	TerrainQuad terrain;
	Node terrainPhysicsNode;
	Material matRock;
	Material matWire;
	boolean wireframe = false;
	protected BitmapText hintText;
	PointLight pl;
	Geometry lightMdl;

	public static void main(String[] args) {
		TerrainTestCollision app = new TerrainTestCollision();
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

			heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 0.25f);
			heightmap.load();

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
		 * terrain will be 513x513. It uses the heightmap we created to generate the height values.
		 */
		terrain = new TerrainQuad("terrain", 65, 513, new Vector3f(1, 1, 1), heightmap.getHeightMap());
		List<Camera> cameras = new ArrayList<Camera>();
		cameras.add(getCamera());
		TerrainLodControl control = new TerrainLodControl(terrain, cameras);
		terrain.addControl(control);
		terrain.setMaterial(matRock);
		terrain.setModelBound(new BoundingBox());
		terrain.updateModelBound();
		terrain.setLocalScale(new Vector3f(2,2,2));
		rootNode.attachChild(terrain);

		/**
		 * Now we use the TerrainPhysicsShapeFactory to generate a heightfield
		 * collision shape for us, and then add it to the physics node.
		 */
		TerrainPhysicsShapeFactory factory = new TerrainPhysicsShapeFactory();
		terrainPhysicsNode = factory.createPhysicsMesh(terrain);
		rootNode.attachChild(terrainPhysicsNode);
		getPhysicsSpace().addAll(terrainPhysicsNode);
		
		// Add 5 physics spheres to the world, with random sizes and positions
		// let them drop from the sky
		for (int i=0; i<5; i++) {
			float r = (float) (5*Math.random());
			PhysicsNode physicsSphere=new PhysicsNode(new SphereCollisionShape(1+r),1);
			float x = (float) (20*Math.random())-20;
			float y = (float) (20*Math.random())-10;
			float z = (float) (20*Math.random())-20;
			physicsSphere.setLocalTranslation(new Vector3f(x,100+y,z));
			physicsSphere.attachDebugShape(getAssetManager());
			rootNode.attachChild(physicsSphere);
			getPhysicsSpace().add(physicsSphere);
		}
		
		// flourescent main light
		pl = new PointLight();
		pl.setColor(new ColorRGBA(0.88f, 0.92f, 0.95f, 1.0f));
		pl.setPosition(new Vector3f(0, 0, 15));
		rootNode.addLight(pl);

		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(new Vector3f(1, -0.5f, -0.1f).normalizeLocal());
		dl.setColor(new ColorRGBA(0.50f, 0.40f, 0.50f, 1.0f));
		rootNode.addLight(dl);


		getCamera().getLocation().y = 25;
		getCamera().setDirection(new Vector3f(-1, 0, -1));
	}

	public void loadHintText() {
		hintText = new BitmapText(guiFont, false);
		hintText.setSize(guiFont.getCharSet().getRenderedSize());
		hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
		//hintText.setText("Hit T to switch to wireframe");
		hintText.setText("");
		guiNode.attachChild(hintText);
	}

	private void setupKeys() {
		flyCam.setMoveSpeed(50);
		inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addListener(actionListener, "wireframe");
	}

	public void update() {
		super.update();
		hintText.setText("cam location: "+getCamera().getLocation());
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
