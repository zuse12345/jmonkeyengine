package jme3test.audio;

import com.jme3.audio.AudioNode;

public class TestWav extends AudioApp {

    private float time = 0;
    private AudioNode src;

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

    public void updateAudioApp(float tpf){
        time += tpf;
        if (time > .1f){
            ar.playSourceInstance(src);
            time = 0;
        }
        
    }

    @Override
    public void initAudioApp(){
        src = new AudioNode(manager, "Sound/Effects/Gun.wav", false);
        src.setLooping(false);
    }

}
