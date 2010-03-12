package jme3test.bullet;

import java.util.logging.Logger;

import com.jme3.app.SimplePhysicsApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.nodes.PhysicsVehicleNode;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;





public class TestPhysicsCar extends SimplePhysicsApplication {
    private static final Logger logger = Logger.getLogger(TestPhysicsCar.class
            .getName());

    //the new player object
    private PhysicsVehicleNode player;
    //protected InputHandler input;


    // Our camera object for viewing the scene
    //The chase camera, this will follow our player as he zooms around the level
    //private ChaseCamera chaser;


	private PhysicsNode tb;

    /**
     * Main entry point of the application
     */
    public static void main(String[] args) {
        TestPhysicsCar app = new TestPhysicsCar();
        // We will load our own "fantastic" Flag Rush logo. Yes, I'm an artist.
		app.start();
    }
	@Override
	public void simpleInitApp() {
        // initialize the camera
        cam.setFrustumPerspective(45.0f, 640 / 480, 1,
                5000);
        cam.setLocation(new Vector3f(0,20,0));

        /** Signal that we've changed our camera's location/frustum. */
        cam.update();

        //Time for a little optimization. We don't need to render back face triangles, so lets
        //not. This will give us a performance boost for very little effort.
        //rootNode.setCullHint(CullHint.Never);

        //Add terrain to the scene
        setupFloor();
        //Light the world
        buildLighting();
        //add the force field fence
        //buildEnvironment();
        //Add the skybox
       // buildSkyBox();
        //Build the player
        buildPlayer();
        //build the chase cam
        //buildChaseCamera();
        //build the player input
        //buildInput();

        // update the scene graph for rendering
       // rootNode.updateGeometricState();

	}


	public void setupFloor() {
		Material mat = manager.loadMaterial("rockwall.j3m");
		mat.selectTechnique("OldGpu");
		mat.getTextureParam("m_DiffuseMap").getValue().setWrap(WrapMode.Repeat);
		mat.getTextureParam("m_NormalMap").getValue().setWrap(WrapMode.Repeat);
		mat.getTextureParam("m_ParallaxMap").getValue()
				.setWrap(WrapMode.Repeat);
		Box floor = new Box(Vector3f.ZERO, 3200, 1f, 3200);
		floor.scaleTextureCoordinates(new Vector2f(320, 320));
		Geometry floorGeom = new Geometry("Floor", floor);
		floorGeom.setMaterial(mat);
		floorGeom.updateModelBound();
		floorGeom.setShadowMode(ShadowMode.Off);
        tb=new PhysicsNode(floorGeom,new MeshCollisionShape(floorGeom.getMesh()),0);
        rootNode.attachChild(tb);
        tb.setLocalTranslation(new Vector3f(0f,-6,0f));
        tb.updateModelBound();
        tb.updateGeometricState();
        getPhysicsSpace().addQueued(tb);
	}






    private void buildPlayer() {
        float r=2f;
    	float x=8,z=10,y=2;
        Material matBox = new Material(manager, "plain_texture.j3md");
        TextureKey keyBox = new TextureKey("signpost_color.jpg", true);
        keyBox.setGenerateMips(true);
        Texture texBox = manager.loadTexture(keyBox);
        texBox.setMinFilter(Texture.MinFilter.Trilinear);
        matBox.setTexture("m_ColorMap", texBox);
        Material mat = new Material(manager, "plain_texture.j3md");
        TextureKey key = new TextureKey("Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);
        //box stand in
        Box b = new Box( new Vector3f(0,r*2+2, 0),x-4.5f,y,z);

        Geometry g= new Geometry("Box",b);
        g.setMaterial(matBox);

        g.getMesh().setBound(new BoundingBox());

        //set the vehicles attributes (these numbers can be thought
        //of as Unit/Second).
        player = new PhysicsVehicleNode(g, new BoxCollisionShape(new Vector3f( x,y,z)));
        float stiffness=200.0f;//200=f1 car
        float compValue=1.0f; //(lower than damp!)
        float dampValue=2.0f;

        player.setMass(1);
        player.updatePhysicsState();
        player.setSuspensionStiffness(stiffness);
        player.setSuspensionCompression(compValue);
       player.setDamping(dampValue,compValue);
       // player.setDirty(true);
        Vector3f wheelDirection=new Vector3f(0,-1,0);
        Vector3f wheelAxle=new Vector3f(-1,0,0);
//         Create four wheels and add them at their locations
        Sphere wheelSphere=new Sphere(8,8,r);
        Geometry wheels1=new Geometry("wheels1",wheelSphere);
        wheels1.setMaterial(mat);
        wheels1.setModelBound(new BoundingBox());
        wheels1.updateModelBound();
        player.addWheel(wheels1, new Vector3f(-x/2,r,-z/2),
                        wheelDirection, wheelAxle, 0.2f, r, true);
       // ((DesktopAssetManager) manager).clearCache();
        Geometry wheels2=new Geometry("wheels1",wheelSphere);
        wheels2.setMaterial(mat);
        wheels2.setModelBound(new BoundingBox());
        wheels2.updateModelBound();
        player.addWheel(wheels2, new Vector3f(x/2,r,-z/2),
                        wheelDirection, wheelAxle, 0.2f, r, true);
        //((DesktopAssetManager) manager).clearCache();
        Geometry wheels3=new Geometry("wheels1",wheelSphere);
        wheels3.setMaterial(matBox);
        wheels3.setModelBound(new BoundingBox());
        wheels3.updateModelBound();
        player.addWheel(wheels3, new Vector3f(x/2,r,z/2),
                        wheelDirection, wheelAxle, 0.2f, r, false);
        //((DesktopAssetManager) manager).clearCache();
        Geometry wheels4=new Geometry("wheels1",wheelSphere);
        wheels4.setMaterial(matBox);
        wheels4.setModelBound(new BoundingBox());
        wheels4.updateModelBound();
        player.addWheel(wheels4, new Vector3f(-x/2,r,z/2),
                        wheelDirection, wheelAxle, 0.2f, r, false);
        player.updatePhysicsState();
       rootNode.attachChild(player);
        //player.setLocalTranslation(new Vector3f(0,10f, 0));
       getPhysicsSpace().add(player);
       player.activate();
       player.accelerate(100);

    }


    /**
     * creates a light for the terrain.
     */
    private void buildLighting() {
        /** Set up a basic, default light. */
        DirectionalLight light = new DirectionalLight();
        light.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setDirection(new Vector3f(1,-1,0));
        rootNode.addLight(light);


    }

  /*  private void buildChaseCamera() {
        Vector3f targetOffset = new Vector3f();
        targetOffset.y = ((BoundingBox)((Geometry) player.getChild(0)).getMesh().getBound()) .getYExtent()* 1.5f;

        chaser = new ChaseCamera(cam, player);
		chaser.registerWithDispatcher(inputManager);

       // chaser.setMaxDistance(8);
       // chaser.setMinDistance(2);
    }*/


    public void simpleUpdate(float tpf) {
        //update the chase camera to handle the player moving around.
       //chaser.updateLogicaState(tpf);
    	Vector3f location=new Vector3f(player.getLocalTranslation());
    	location.y+=100;
    	location.x+=10;
    	location.z+=10;
       System.out.println(player.getLocalTranslation());
       cam.setLocation(location);
       cam.lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);
      //player.updatePhysicsState();
        //player.apply(1);
      // player.accelerate(10);
       //player.applyCentralForce( new Vector3f(10,0,0));
      // player.updateLogicalState(tpf);
      // player.setLinearVelocity(new Vector3f(10,0,0));
        //We don't want the chase camera to go below the world, so always keep
        //it 2 units above the level.
        if(cam.getLocation().y < 2) {
            cam.getLocation().y +=  2;
        }

    }


}