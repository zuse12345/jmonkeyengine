package g3dtest.audio;

import com.g3d.audio.AudioRenderer;
import com.g3d.audio.joal.JoalAudioRenderer;
import com.g3d.renderer.Camera;
import com.g3d.asset.AssetManager;
import com.g3d.system.G3DSystem;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioApp {

    private static final float UPDATE_RATE = 0.01f;

    protected AssetManager manager;
    protected Camera listener;
    protected AudioRenderer ar;

    public AudioApp(){
        G3DSystem.initialize();
        manager = new AssetManager(true);

        ar = new JoalAudioRenderer();
        ar.initialize();

        listener = new Camera(1,1);
        ar.setListener(listener);
    }

    public void initAudioApp(){
    }

    public void updateAudioApp(float tpf){
    }

    public void start(){
        initAudioApp();

        while (true){
            updateAudioApp(UPDATE_RATE);
            ar.update(UPDATE_RATE);

            try{
                Thread.sleep((int) (UPDATE_RATE * 1000f));
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
