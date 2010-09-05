package jme3test.helloworld;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * Example 12 - how to give objects physical properties so they bounce and fall.
 * @author double1984, reformatted and javadocced by zathras
 */
public class HelloPhysics extends SimpleBulletApplication {
  
    public static void main(String args[]) {
        HelloPhysics app = new HelloPhysics();
        app.start();
    }

    /** Activate custom rendering of shadows */
    BasicShadowRenderer bsr;

    /** geometries and collisions shapes for bricks and bullet */
    private static final Box    brick;
    private static final BoxCollisionShape boxCollisionShape;
    private static final Sphere bullet;
    private static final SphereCollisionShape bulletCollisionShape;

    /** brick dimensions */
    private static final float brickLength = 0.48f;
    private static final float brickWidth  = 0.24f;
    private static final float brickHeight = 0.12f;

    /** Materials */
    Material wall_mat;
    Material bullet_mat;
    Material floor_mat;

    static {
        /** initializing the bullet geometry that is reused later */
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape=new SphereCollisionShape(0.4f);
        /** initializing the brick geometry that is reused later */
        brick = new Box(Vector3f.ZERO, brickLength, brickHeight, brickWidth);
        brick.scaleTextureCoordinates(new Vector2f(1f, .5f));
        boxCollisionShape = new BoxCollisionShape(new Vector3f(brickLength, brickHeight, brickWidth));
    }

    @Override
    public void simpleInitApp() {
        /** Set up camera */
        this.cam.setLocation(new Vector3f(0, 6f, 6f));
        cam.lookAt(Vector3f.ZERO, new Vector3f(0, 1, 0));
        cam.setFrustumFar(15);
        /** Add shooting action */
        inputManager.addMapping("shoot", new MouseButtonTrigger(0));
        inputManager.addListener(actionListener, "shoot");
        /** Initialize the scene and physics space */
        initMaterials();
        initWall();
        initFloor();
        initCrossHairs();
        this.getPhysicsSpace().setAccuracy(0.005f);
        /** Activate custom shadows */
        rootNode.setShadowMode(ShadowMode.Off);
        bsr = new BasicShadowRenderer(assetManager, 256);
        bsr.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        viewPort.addProcessor(bsr);
    }

    /**
     * Every time the shoot action is triggered, a new bullet is produced.
     * The bullet is set up to fly from the camera position in the camera direction.
     */
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("shoot") && !keyPressed) {
                makeBullet();
            }
        }
    };

    /** Initialize the materials used in this scene. */
    public void initMaterials() {
        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wall_mat.setTexture("m_ColorMap", tex);

        bullet_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        bullet_mat.setTexture("m_ColorMap", tex2);

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key3 = new TextureKey("Textures/Terrain/Pond/Pond.png");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(WrapMode.Repeat);
        floor_mat.setTexture("m_ColorMap", tex3);
    }

    /** Make a solid floor and add it to the scene. */
    public void initFloor() {
        Box floorBox = new Box(Vector3f.ZERO, 10f, 0.1f, 5f);
        floorBox.scaleTextureCoordinates(new Vector2f(3, 6));
        Geometry floor = new Geometry("floor", floorBox);
        floor.setMaterial(floor_mat);
        floor.setShadowMode(ShadowMode.Recieve);
        PhysicsNode floorNode = new PhysicsNode(
          floor,
          new BoxCollisionShape(new Vector3f(10f, 0.1f, 5f)),
          0);
        floorNode.setLocalTranslation(0, -0.1f, 0);
        floorNode.updateGeometricState();
        this.rootNode.attachChild(floorNode);
        this.getPhysicsSpace().add(floorNode);
    }

    /** A loop that builds a wall out of individual bricks. */
    public void initWall() {
        float startpt = brickLength / 4;
        float height = 0;
        for (int j = 0; j < 15; j++) {
            for (int i = 0; i < 4; i++) {
                Vector3f vt = new Vector3f(i * brickLength * 2 + startpt, brickHeight + height, 0);
                makeBrick(vt);
            }
            startpt = -startpt;
            height += 2 * brickHeight;
        }
    }

    /** This method creates one individual physical brick. */
    public void makeBrick(Vector3f ori) {
        /** create a new brick */
        Geometry reBoxg = new Geometry("brick", brick);
        reBoxg.setMaterial(wall_mat);
        PhysicsNode brickNode = new PhysicsNode(
          reBoxg,            // geometry
          boxCollisionShape, // collision shape
          1.5f);             // mass
        /** position the brick and activate shadows */
        brickNode.setLocalTranslation(ori);
        brickNode.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(brickNode);
        getPhysicsSpace().add(brickNode);
    }

    /** This method creates one individual physical bullet.
     *  By defaul, the bullet is accelerated and flies
     *  from the camera position in the camera direction.*/
    public void makeBullet() {
        /** create a new bullet  */
        Geometry bulletg = new Geometry("bullet", bullet);
        bulletg.setMaterial(bullet_mat);
        PhysicsNode bulletNode = new PhysicsNode(
             bulletg,               // geometry
             bulletCollisionShape,  // collision shape
             1.0f);                 // mass
        /** position the bullet and activate shadows */
        bulletNode.setLocalTranslation(cam.getLocation());
        bulletNode.updateGeometricState();
        bulletNode.setShadowMode(ShadowMode.CastAndRecieve);
        /** Accelerate the bullet and attach it to the scene. */
        bulletNode.setLinearVelocity(cam.getDirection().mult(25));
        rootNode.attachChild(bulletNode);
        getPhysicsSpace().add(bulletNode);
    }

    /** A plus sign used as crosshairs to help the player with aiming.*/
    protected void initCrossHairs() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }
}
