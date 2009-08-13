package com.g3d.audio;

import com.g3d.renderer.Camera;

public interface AudioRenderer {

    public void setListener(Camera listener);

    public void playSourceInstance(AudioSource src);
    public void playSource(AudioSource src);
    public void pauseSource(AudioSource src);
    public void stopSource(AudioSource src);

    public void updateSource(AudioSource src);
    public void deleteSource(AudioSource src);

    public void updateAudioData(AudioData ad);
    public void deleteAudioData(AudioData ad);

    public void initialize();
    public void update(float tpf);
    public void cleanup();
}
