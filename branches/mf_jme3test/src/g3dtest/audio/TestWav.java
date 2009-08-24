package g3dtest.audio;

import com.g3d.audio.AudioSource;

public class TestWav extends AudioApp {
    
    public static void main(String[] args){
        TestWav test = new TestWav();
        test.start();
    }

    @Override
    public void initAudioApp(){
        AudioSource src = new AudioSource(manager, "gun.wav", false);
        src.setLooping(true);
        ar.playSource(src);
    }

}
