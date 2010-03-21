package com.jme3.audio;

import com.jme3.asset.AssetManager;
import com.jme3.asset.AudioKey;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;

public class AudioSource implements Cloneable, Savable {

    protected boolean loop = false;
    protected float volume = 1;
    protected float pitch = 1;
    protected float timeOffset = 0;
    protected Filter dryFilter;
    protected AudioKey key;
    
    protected transient AudioData data = null;
    protected transient Status status = Status.Stopped;
    protected transient int channel = -1;

    public enum Status {
        Playing,
        Paused,
        Stopped,
    }

    public AudioSource(){
    }

    public AudioSource(AudioData ad, AudioKey key){
        this();
        setAudioData(ad, key);
    }

    public AudioSource(AssetManager manager, String name, boolean stream){
        this();
        this.key = new AudioKey(name, stream);
        this.data = (AudioData) manager.loadContent(key);
    }
    
    public AudioSource(AssetManager manager, String name){
        this(manager, name, false);
    }

    public void setChannel(int channel){
        if (status != Status.Stopped)
            throw new IllegalStateException("Can only set source id when stopped");

        this.channel = channel;
    }

    public int getChannel(){
        return channel;
    }

    public Filter getDryFilter() {
        return dryFilter;
    }

    public void setDryFilter(Filter dryFilter) {
        if (this.dryFilter != null)
            throw new IllegalStateException("Filter already set");

        this.dryFilter = dryFilter;
    }
    
    public void setAudioData(AudioData ad, AudioKey key){
        if (data != null)
            throw new IllegalStateException("Cannot change data once its set");
        
        data = ad;
        this.key = key;
    }

    public AudioData getAudioData() {
        return data;
    }

    public Status getStatus(){
        return status;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public boolean isLooping() {
        return loop;
    }

    public void setLooping(boolean loop) {
        this.loop = loop;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        if (pitch < 0.5f || pitch > 2.0f)
            throw new IllegalArgumentException("Pitch must be between 0.5 and 2.0");

        this.pitch = pitch;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0f)
            throw new IllegalArgumentException("Volume cannot be negative");

        this.volume = volume;
    }

    public float getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(float timeOffset) {
        if (timeOffset < 0f)
            throw new IllegalArgumentException("Time offset cannot be negative");

        this.timeOffset = timeOffset;
    }

    @Override
    public AudioSource clone(){
        try{
            return (AudioSource) super.clone();
        }catch (CloneNotSupportedException ex){
            return null;
        }
    }

    /*
     * protected boolean loop = false;
    protected float volume = 1;
    protected float pitch = 1;
    protected float timeOffset = 0;
    protected Filter dryFilter;
    protected AudioKey key;*/

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(key, "key", null);
        oc.write(loop, "looping", false);
        oc.write(volume, "volume", 1);
        oc.write(pitch, "pitch", 1);
        oc.write(timeOffset, "time_offset", 0);
        oc.write(dryFilter, "dry_filter", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        key =   (AudioKey) ic.readSavable("key", null);
        loop = ic.readBoolean("looping", false);
        volume = ic.readFloat("volume", 1);
        pitch = ic.readFloat("pitch", 1);
        timeOffset = ic.readFloat("time_offset", 0);
        dryFilter = (Filter) ic.readSavable("dry_filter", null);

    }

    public String toString(){
        String ret = getClass().getSimpleName() +
                     "[status="+status;
        if (volume != 1f)
            ret += ", vol="+volume;
        if (pitch != 1f)
            ret += ", pitch="+pitch;
        return ret + "]";
    }

}
