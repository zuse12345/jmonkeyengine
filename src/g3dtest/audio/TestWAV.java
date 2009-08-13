package g3dtest.audio;

import com.g3d.audio.AudioBuffer;
import com.g3d.audio.AudioRenderer;
import com.g3d.audio.AudioSource;
import com.g3d.audio.joal.JoalAudioRenderer;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.res.ContentKey;
import com.g3d.res.ContentManager;
import com.g3d.system.G3DSystem;
import net.java.games.joal.eax.EAX;

public class TestWAV {
    
    private static float angle = -10;
    private static float rate = 1;

    private static AudioSource src;
    private static AudioRenderer ar;

    public static void main(String[] args){
        G3DSystem.initialize();

        ContentManager man = new ContentManager(true);
        AudioBuffer ab = (AudioBuffer) man.loadContent(new ContentKey("footsteps.wav"));

        src = new AudioSource(ab);
        src.setLooping(true);

        Camera listener = new Camera(1,1);

        ar = new JoalAudioRenderer();
        ar.initialize();
        ar.setListener(listener);

        src.setPosition(new Vector3f(0, 0, 20));
        ar.playSource(src);

        while (true){
            update();
            ar.update(0.1f);
        }
    }

    public static void update(){
//        angle += rate;
//        if (angle > 10){
//            rate = -rate;
//            angle -= 0.1f;
//        }else if (angle < 10){
//            rate = -rate;
//            angle += 0.1f;
//        }

//        src.setPosition(new Vector3f(0, 0, 0));

//        angle %= 360;
//
//        Vector3f pos = new Vector3f();
//        pos.x = FastMath.cos(angle * FastMath.DEG_TO_RAD) * 10;
//        pos.z = FastMath.sin(angle * FastMath.DEG_TO_RAD) * 3;
//
//        Vector3f prevPos = src.getPosition().clone();
//        Vector3f vel = pos.subtract(prevPos);
////        vel.multLocal(100);
//        src.setPosition(pos);
//        src.setVelocity(vel);
        
        try{
            Thread.sleep(10);
        }catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

}
