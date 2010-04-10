package jme3test.model.anim;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.binding.BindingListener;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.control.ControlType;
import com.jme3.scene.debug.SkeletonDebugger;
import com.jme3.scene.plugins.ogre.OgreMaterialList;
import com.jme3.scene.plugins.ogre.OgreMeshKey;

public class TestAnimBlendBug extends SimpleApplication implements BindingListener {

//    private AnimControl control;
    private AnimChannel channel1, channel2;
    private String[] animNames;

    private float blendTime = 0.5f;
    private float lockAfterBlending =  blendTime + 0.25f;
    private float blendingAnimationLock;

    public static void main(String[] args) {
        TestAnimBlendBug app = new TestAnimBlendBug();
        app.start();
    }

    public void onBinding(String binding, float value) {
        if (binding.equals("One")){
            channel1.setAnim(animNames[4], blendTime);
            channel2.setAnim(animNames[4], 0);
            channel1.setSpeed(0.25f);
            channel2.setSpeed(0.25f);
            blendingAnimationLock = lockAfterBlending;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Is there currently a blending underway?
        if (blendingAnimationLock > 0f) {
            blendingAnimationLock -= tpf;
        }
    }

    @Override
    public void simpleInitApp() {
        inputManager.registerKeyBinding("One", KeyInput.KEY_1);
        inputManager.addBindingListener(this);

        flyCam.setMoveSpeed(100f);
        cam.setLocation( new Vector3f( 0f, 150f, -325f ) );
        cam.lookAt( new Vector3f( 0f, 100f, 0f ), Vector3f.UNIT_Y );

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, 1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);

        OgreMaterialList matList = (OgreMaterialList) manager.loadContent("ninja.material");
        OgreMeshKey key = new OgreMeshKey("ninja.meshxml", matList);
        Node model1 = (Node) manager.loadContent(key);
        Node model2 = model1.clone();

        model1.setLocalTranslation(-60, 0, 0);
        model2.setLocalTranslation(60, 0, 0);

        AnimControl control1 = (AnimControl) model1.getControl(ControlType.BoneAnimation);
        animNames = control1.getAnimationNames().toArray(new String[0]);
        channel1 = control1.createChannel();
        
        AnimControl control2 = (AnimControl) model2.getControl(ControlType.BoneAnimation);
        channel2 = control2.createChannel();

        rootNode.attachChild(model1);
        rootNode.attachChild(model2);
    }

}
