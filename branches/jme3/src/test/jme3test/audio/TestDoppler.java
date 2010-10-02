package jme3test.audio;

import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;

/**
 * Test Doppler Effect
 */
public class TestDoppler extends AudioApp {

    private AudioNode ufo;

    private float location = 0;
    private float rate = 1;

    public static void main(String[] args){
        TestDoppler test = new TestDoppler();
        test.start();
    }

    @Override
    public void initAudioApp(){
        ufo  = new AudioNode(manager, "Sound/Effects/Beep.ogg", false);
        ufo.setPositional(true);
        ufo.setLooping(true);
        ar.playSource(ufo);
    }

    @Override
    public void updateAudioApp(float tpf){
        // move the location variable left and right
        if (location > 10){
            location = 10;
            rate = -rate;
            ufo.setVelocity(new Vector3f(rate*10, 0, 0));
        }else if (location < -10){
            location = -10;
            rate = -rate;
            ufo.setVelocity(new Vector3f(rate*10, 0, 0));
        }else{
            location += rate * tpf * 10;
        }
        ufo.setLocalTranslation(location, 0, 2);
    }

}
