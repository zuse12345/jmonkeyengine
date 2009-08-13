package com.g3d.audio;

import com.g3d.audio.AudioData.DataType;
import java.nio.ByteBuffer;

public class AudioBuffer extends AudioData {

    protected ByteBuffer audioData;

    public AudioBuffer(){
    }

    public DataType getDataType() {
        return DataType.Buffer;
    }

    public float getDuration(){
        int bytesPerSec = (bitsPerSample / 8) * channels * sampleRate;
        if (audioData != null)
            return (float) audioData.capacity() / bytesPerSec;
        else
            return Float.NaN; // unknown
    }

    @Override
    public String toString(){
        return getClass().getSimpleName() +
               "[id="+id+", ch="+channels+", bits="+bitsPerSample +
               ", rate="+sampleRate+", duration="+getDuration()+"]";
    }

    public void updateData(ByteBuffer data){
        this.audioData = data;
        updateNeeded = true;
    }

    public ByteBuffer getData(){
        return audioData;
    }

    public void resetObject() {
        id = -1;
        setUpdateNeeded();
    }

    public void deleteObject(AudioRenderer ar) {
        ar.deleteAudioData(this);
    }

}
