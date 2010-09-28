package com.jme3.network.sync;

public class MovingAverage {

    private long[] samples;
    private long sum;
    private int count, index;

    public MovingAverage(int numSamples){
        samples = new long[numSamples];
    }

    public void add(long sample){
        sum = sum - samples[index] + sample;
        samples[index++] = sample;
        if (index > count){
            count = index;
        }
        if (index >= samples.length){
            index = 0;
        }
    }

    public long getAverage(){
        if (count == 0)
            return 0;
        else
            return (long) ((float) sum / (float) count);
    }

}
