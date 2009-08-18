package g3dtest.audio;

import com.g3d.audio.AudioSource;

public class TestOgg extends AudioApp {

    public static void main(String[] args){
        TestOgg test = new TestOgg();
        test.start();
    }

    @Override
    public void initAudioApp(){
        AudioSource src = new AudioSource(manager, "footsteps.ogg", true);
//        src.setLooping(true);
        ar.playSource(src);
    }

}
