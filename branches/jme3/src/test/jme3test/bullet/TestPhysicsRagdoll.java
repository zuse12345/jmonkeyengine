package jme3test.bullet;

import com.jme3.animation.AnimControl;
import com.jme3.app.SimplePhysicsApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.PhysicsRagdollControl;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.ControlType;
import com.jme3.scene.debug.SkeletonDebugger;
import com.jme3.scene.plugins.ogre.MeshLoader;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class TestPhysicsRagdoll  extends SimplePhysicsApplication {

    public static void main(String[] args){
        TestPhysicsRagdoll app = new TestPhysicsRagdoll();
        app.start();
    }

    public void simpleInitApp() {

        Material mat = new Material(manager, "plain_texture.j3md");
        TextureKey key = new TextureKey("Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        // the floor, does not move (mass=0)
        Geometry geom5=new Geometry("box2",new Box(Vector3f.ZERO,100f,1f,100f));
        geom5.setMaterial(mat);
        geom5.updateGeometricState();
        PhysicsNode node3=new PhysicsNode(geom5,new BoxCollisionShape(new Vector3f(100f,1f,100f)),0);
        node3.setLocalTranslation(new Vector3f(0f,-6,0f));
        node3.updateModelBound();
        rootNode.attachChild(node3);
        getPhysicsSpace().add(node3);
        
        Node model = (Node)MeshLoader.loadModel(manager, "OTO.meshxml", "OTO.material");
//        Node model = (Node)MeshLoader.loadModel(manager, "ninja.meshxml", "ninja.material");

        //debug view
//        AnimControl control= (AnimControl) model.getControl(ControlType.BoneAnimation);
//        SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton());
//        Material mat2 = new Material(manager, "wire_color.j3md");
//        mat.setColor("m_Color", ColorRGBA.Green);
//        mat.getAdditionalRenderState().setDepthTest(false);
//        skeletonDebug.setMaterial(mat2);
//        model.attachChild(skeletonDebug);

        //Note: PhysicsRagdollControl is still TODO, constructor will change
        PhysicsRagdollControl ragdoll = new PhysicsRagdollControl(this,getPhysicsSpace());
        ragdoll.setSpatial(model);
        model.setControl(ragdoll);
//        model.setLocalScale(0.2f);
        rootNode.attachChild(model);
    }

    public Spatial createCylinder(float radius, float height){
        Box b = new Box(Vector3f.ZERO, radius, height/2.0f,  radius);
//        Cylinder b=new Cylinder(8,8,radius,height);
        Geometry geom = new Geometry("Box", b);
        geom.updateModelBound();

        Material mat = new Material(manager, "plain_texture.j3md");
        TextureKey key = new TextureKey("Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture tex = manager.loadTexture(key);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("m_ColorMap", tex);

        geom.setMaterial(mat);
        
        return geom;


    }

    public Node getRootNode() {
        return rootNode;
    }

}
