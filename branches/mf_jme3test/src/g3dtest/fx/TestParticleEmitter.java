package g3dtest.fx;

import com.g3d.app.SimpleApplication;
import com.g3d.effect.ParticleEmitter;
import com.g3d.effect.ParticleMesh.Type;
import com.g3d.material.Material;
import com.g3d.system.AppSettings;

public class TestParticleEmitter extends SimpleApplication {

    public static void main(String[] args){
        TestParticleEmitter app = new TestParticleEmitter();
        app.setSettings(new AppSettings(AppSettings.Template.Default800x600));
        app.start();
    }

    @Override
    public void simpleInitApp() {
//        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
//        lightMdl.setMaterial(manager.loadMaterial("red_color.j3m"));
//        rootNode.attachChild(lightMdl);

        ParticleEmitter emit = new ParticleEmitter("Emitter", Type.Triangle, 200, 15, 1);
        emit.setCamera(cam);

        Material mat = new Material(manager, "point_sprite.j3md");
        mat.setTexture("m_Texture", manager.loadTexture("zsmoke.png"));
        emit.setMaterial(mat);

        rootNode.attachChild(emit);
    }

}
