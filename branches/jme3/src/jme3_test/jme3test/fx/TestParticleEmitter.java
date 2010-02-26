package jme3test.fx;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.EmitterSphereShape;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;

public class TestParticleEmitter extends SimpleApplication {

    public static void main(String[] args){
        TestParticleEmitter app = new TestParticleEmitter();
        app.start();
    }

    @Override
    public void simpleInitApp() {
//        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
//        lightMdl.setMaterial(manager.loadMaterial("red_color.j3m"));
//        rootNode.attachChild(lightMdl);

        ParticleEmitter emit = new ParticleEmitter("Emitter", Type.Triangle, 200);
        emit.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        emit.setGravity(0);
        emit.setLowLife(5);
        emit.setHighLife(10);
        emit.setStartVel(new Vector3f(0, 0, 0));
        emit.setImagesX(15);
        emit.setCamera(cam);
        Material mat = new Material(manager, "point_sprite.j3md");
        mat.setTexture("m_Texture", manager.loadTexture("zsmoke.png"));
        emit.setMaterial(mat);

        rootNode.attachChild(emit);
    }

}
