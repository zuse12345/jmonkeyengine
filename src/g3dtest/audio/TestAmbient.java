package g3dtest.audio;

import com.g3d.audio.AudioSource;
import com.g3d.audio.Environment;
import com.g3d.math.FastMath;
import com.g3d.math.Vector3f;

public class TestAmbient extends AudioApp {

    private AudioSource river, waves, nature;
    private float time = 0;
    private float nextTime = 1;

    public static void main(String[] args){
        TestAmbient test = new TestAmbient();
        test.start();
    }

    @Override
    public void initAudioApp(){
        waves  = new AudioSource(manager, "ocean_waves.ogg", true);
        nature = new AudioSource(manager, "nature.ogg", true);
        
//        river  = new AudioSource(manager, "river.ogg");

        float[] eax = new float[]
            {15,	38.0f,	0.300f,	-1000,	-3300,	0,		1.49f,	0.54f,	1.00f,  -2560,	0.162f, 0.00f,0.00f,0.00f,	-229,	0.088f,		0.00f,0.00f,0.00f,	0.125f, 1.000f, 0.250f, 0.000f, -5.0f,  5000.0f,	250.0f, 0.00f,	0x3f }
            ;

        Environment env = new Environment(eax);
        ar.setEnvironment(env);

        nature.setPositional(false);
        nature.setReverbEnabled(false);
        ar.playSource(nature);

        waves.setPosition(new Vector3f(4, -1, 30));
        waves.setReverbEnabled(true);
        waves.setMaxDistance(1500);
        waves.setPositional(true);
        ar.playSource(waves);
    }

    @Override
    public void updateAudioApp(float tpf){
        time += tpf;

        if (time > nextTime){
            
            time = 0;
            nextTime = FastMath.nextRandomFloat() * 2 + 0.5f;
        }
    }

}
