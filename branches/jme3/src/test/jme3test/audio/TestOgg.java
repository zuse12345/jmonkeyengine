package jme3test.audio;

import com.jme3.audio.AudioSource;
import com.jme3.audio.LowPassFilter;
import com.jme3.audio.PointAudioSource;

public class TestOgg extends AudioApp {

    private AudioSource src;

    public static void main(String[] args){
        TestOgg test = new TestOgg();
        test.start();
    }

    @Override
    public void initAudioApp(){
        System.out.println("Playing without filter");
        src = new PointAudioSource(manager, "footsteps.ogg", true);
        ar.playSource(src);
    }

    public void updateAudioApp(float tpf){
        if (src.getStatus() != AudioSource.Status.Playing){
            ar.deleteAudioData(src.getAudioData());

            System.out.println("Playing with low pass filter");
            src = new PointAudioSource(manager, "footsteps.ogg", true);
            src.setDryFilter(new LowPassFilter(1f, .5f));
            ar.playSource(src);
        }
    }

}
