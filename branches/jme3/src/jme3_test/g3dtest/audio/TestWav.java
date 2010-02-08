package g3dtest.audio;

import com.g3d.audio.AudioSource;

public class TestWav extends AudioApp {
    
    public static void main(String[] args){
        TestWav test = new TestWav();
        test.start();
    }

    public static final void sleep(float time){
        try{
            Thread.sleep((long) (time * 1000));
        }catch (InterruptedException ex){
        }
    }

    @Override
    public void initAudioApp(){
        AudioSource src = new AudioSource(manager, "gun.wav", false);
        src.setLooping(true);

        ar.playSource(src);
        sleep(2);
        ar.stopSource(src);
        sleep(1);
        ar.playSource(src);
        sleep(1);
        ar.pauseSource(src);
        sleep(2);
        ar.playSource(src);
    }

}
