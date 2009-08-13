package com.g3d.audio;

import com.g3d.math.FastMath;
import org.lwjgl.openal.AL10;

public class Environment {

    private float airAbsorbGainHf   = 0.99426f;
    private float roomRolloffFactor = 0;

    private float decayTime         = 1.49f;
    private float decayHFRatio      = 0.54f;

    private float density           = 1.0f;
    private float diffusion         = 0.3f;

    private float gain              = 0.316f;
    private float gainHf            = 0.022f;

    private float lateReverbDelay   = 0.088f;
    private float lateReverbGain    = 0.768f;

    private float reflectDelay      = 0.162f;
    private float reflectGain       = 0.052f;

    private boolean decayHfLimit    = true;

    private static final float eaxDbToAmp(float eaxDb){
        float dB = eaxDb / 2000f;
        return FastMath.pow(10f, dB);
    }

    public Environment(){
    }

    public Environment(float[] e){
        if (e.length != 28)
            throw new IllegalArgumentException("Not an EAX preset");

        // skip env id
        // e[0]
        // skip room size
        // e[1]

        diffusion = e[2];
        gain = eaxDbToAmp(e[3]); // convert
        gainHf = eaxDbToAmp(e[4]) / eaxDbToAmp(e[5]); // convert
        decayTime = e[6];
        decayHFRatio = e[7] / e[8];
        reflectGain = eaxDbToAmp(e[9]); // convert
        reflectDelay = e[10];

        // skip 3 pan values
        // e[11] e[12] e[13]

        lateReverbGain = eaxDbToAmp(e[14]); // convert
        lateReverbDelay = e[15];

        // skip 3 pan values
        // e[16] e[17] e[18]

        // skip echo time, echo damping, mod time, mod damping
        // e[19] e[20] e[21] e[22]

        airAbsorbGainHf = eaxDbToAmp(e[23]);

        // skip HF Reference and LF Reference
        // e[24] e[25]

        roomRolloffFactor = e[26];

        // skip flags
        // e[27]
    }

    public float getAirAbsorbGainHf() {
        return airAbsorbGainHf;
    }

    public void setAirAbsorbGainHf(float airAbsorbGainHf) {
        this.airAbsorbGainHf = airAbsorbGainHf;
    }

    public float getDecayHFRatio() {
        return decayHFRatio;
    }

    public void setDecayHFRatio(float decayHFRatio) {
        this.decayHFRatio = decayHFRatio;
    }

    public boolean isDecayHfLimit() {
        return decayHfLimit;
    }

    public void setDecayHfLimit(boolean decayHfLimit) {
        this.decayHfLimit = decayHfLimit;
    }

    public float getDecayTime() {
        return decayTime;
    }

    public void setDecayTime(float decayTime) {
        this.decayTime = decayTime;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public float getDiffusion() {
        return diffusion;
    }

    public void setDiffusion(float diffusion) {
        this.diffusion = diffusion;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getGainHf() {
        return gainHf;
    }

    public void setGainHf(float gainHf) {
        this.gainHf = gainHf;
    }

    public float getLateReverbDelay() {
        return lateReverbDelay;
    }

    public void setLateReverbDelay(float lateReverbDelay) {
        this.lateReverbDelay = lateReverbDelay;
    }

    public float getLateReverbGain() {
        return lateReverbGain;
    }

    public void setLateReverbGain(float lateReverbGain) {
        this.lateReverbGain = lateReverbGain;
    }

    public float getReflectDelay() {
        return reflectDelay;
    }

    public void setReflectDelay(float reflectDelay) {
        this.reflectDelay = reflectDelay;
    }

    public float getReflectGain() {
        return reflectGain;
    }

    public void setReflectGain(float reflectGain) {
        this.reflectGain = reflectGain;
    }

    public float getRoomRolloffFactor() {
        return roomRolloffFactor;
    }

    public void setRoomRolloffFactor(float roomRolloffFactor) {
        this.roomRolloffFactor = roomRolloffFactor;
    }
}
