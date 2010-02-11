package g3dtest.audio;

import com.g3d.audio.AudioRenderer;
import com.g3d.renderer.Camera;
import com.g3d.asset.AssetManager;
import com.g3d.audio.Listener;
import com.g3d.system.AppSettings;
import com.g3d.system.G3DSystem;

public class AudioApp {

    private static final float UPDATE_RATE = 0.01f;

    protected AssetManager manager;
    protected Listener listener;
    protected AudioRenderer ar;

    public AudioApp(){
        AppSettings settings = new AppSettings(true);
        settings.setRenderer(null); // force dummy renderer (?)
        settings.setAudioRenderer("LWJGL");
        ar = G3DSystem.newAudioRenderer(settings);
        ar.initialize();
        manager = G3DSystem.newAssetManager();

        listener = new Listener();
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
