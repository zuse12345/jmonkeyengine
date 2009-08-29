package com.g3d.audio;

import com.g3d.math.Vector3f;
import com.g3d.asset.AssetManager;

public class AudioSource extends ALObject {

    private Vector3f position = new Vector3f();
    private Vector3f velocity = new Vector3f();

    private boolean positional = true;
    private boolean loop = false;
    private boolean reverbEnabled = true;
    private float volume = 1;
    private float pitch = 1;
    private float timeOffset = 0;
    private float maxDistance = 20; // 20 meters

    private AudioData data = null;
    private Status status = Status.Stopped;

    public enum Status {
        Playing,
        Paused,
        Stopped,
    }

    public AudioSource(){
    }

    public AudioSource(AudioData ad){
        this();
        setAudioData(ad);
    }

    public AudioSource(AssetManager manager, String name, boolean stream){
        this(manager.loadAudio(name, stream));
    }
    
    public AudioSource(AssetManager manager, String name){
        this(manager, name, false);
    }
    
    public void setAudioData(AudioData ad){
        if (data != null)
            throw new IllegalStateException("AudioData already set");

        data = ad;
        setUpdateNeeded();
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

    public boolean isPositional() {
        return positional;
    }

    public void setPositional(boolean positional) {
        this.positional = positional;
    }

    public boolean isReverbEnabled() {
        return reverbEnabled;
    }

    public void setReverbEnabled(boolean reverbEnabled) {
        this.reverbEnabled = reverbEnabled;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
        setUpdateNeeded();
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
        setUpdateNeeded();
    }

    public boolean isLooping() {
        return loop;
    }

    public void setLooping(boolean loop) {
        this.loop = loop;
        setUpdateNeeded();
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        if (pitch < 0.5f || pitch > 2.0f)
            throw new IllegalArgumentException("Pitch must be between 0.5 and 2.0");

        setUpdateNeeded();
        this.pitch = pitch;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0f)
            throw new IllegalArgumentException("Volume cannot be negative");

        setUpdateNeeded();
        this.volume = volume;
    }

    public float getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(float timeOffset) {
        this.timeOffset = timeOffset;
        setUpdateNeeded();
    }

    private String toString(Vector3f v){
        return String.format("%1.0f,%1.0f,%1.0f", v.x, v.y, v.z);
    }

    public String toString(){
        String ret = getClass().getSimpleName() +
                     "[id="+id+", status="+status;
        if (positional){
            ret += ", pos="+toString(position)+", vel="+toString(velocity);
        }
        if (volume != 1f)
            ret += ", vol="+volume;
        if (pitch != 1f)
            ret += ", pitch="+pitch;
        return ret + "]";
    }

    @Override
    public void resetObject() {
        id = -1;
        status = Status.Stopped;
        setUpdateNeeded();
        // parent data is reset automatically
    }

    @Override
    public void deleteObject(AudioRenderer r) {
        r.deleteSource(this);
    }

}
