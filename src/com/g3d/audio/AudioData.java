package com.g3d.audio;

public abstract class AudioData extends ALObject {

    protected int sampleRate;
    protected int channels;
    protected int bitsPerSample;

    public enum DataType {
        Buffer,
        Stream
    }

    public abstract DataType getDataType();

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public int getChannels() {
        return channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setupFormat(int channels, int bitsPerSample, int sampleRate){
        if (id != -1)
            throw new IllegalStateException("Already set up");

        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
        this.sampleRate = sampleRate;
    }

}
