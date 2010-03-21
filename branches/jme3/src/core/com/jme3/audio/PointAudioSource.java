package com.jme3.audio;

import com.jme3.asset.AssetManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import java.io.IOException;

public class PointAudioSource extends AudioSource {

    protected Vector3f position = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    protected boolean reverbEnabled = true;
    protected float maxDistance = 20; // 20 meters
    protected float refDistance = 10; // 10 meters
    protected Filter reverbFilter;
    
    public PointAudioSource(AssetManager manager, String name, boolean stream){
        super(manager, name, stream);
    }

    public PointAudioSource(AssetManager manager, String name){
        super(manager, name);
    }

    public PointAudioSource(){
    }

    public boolean isReverbEnabled() {
        return reverbEnabled;
    }

    public void setReverbEnabled(boolean reverbEnabled) {
        this.reverbEnabled = reverbEnabled;
    }

    public Filter getReverbFilter() {
        return reverbFilter;
    }

    public void setReverbFilter(Filter reverbFilter) {
        if (this.reverbFilter != null)
            throw new IllegalStateException("Filter already set");

        this.reverbFilter = reverbFilter;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        if (maxDistance < 0)
            throw new IllegalArgumentException("Max distance cannot be negative");

        this.maxDistance = maxDistance;
    }

    public float getRefDistance() {
        return refDistance;
    }

    public void setRefDistance(float refDistance) {
        if (refDistance < 0)
            throw new IllegalArgumentException("Reference distance cannot be negative");

        this.refDistance = refDistance;
    }
    
    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }

    private String toString(Vector3f v){
        return String.format("%1.0f,%1.0f,%1.0f", v.x, v.y, v.z);
    }

    /*
     * protected Vector3f position = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    protected boolean reverbEnabled = true;
    protected float maxDistance = 20; // 20 meters
    protected float refDistance = 10; // 10 meters
    protected Filter reverbFilter;*/

    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(position, "position", null);
//        oc.write(velocity, "velocity", null);
        oc.write(reverbEnabled, "reverb_enabled", false);
        oc.write(reverbFilter, "reverb_filter", null);
        oc.write(maxDistance, "max_distance", 20);
        oc.write(refDistance, "ref_distance", 10);

    }
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        position = (Vector3f) ic.readSavable("position", null);
        reverbEnabled = ic.readBoolean("reverb_enabled", false);
        reverbFilter = (Filter) ic.readSavable("reverb_filter", null);
        maxDistance = ic.readFloat("max_distance", 20);
        refDistance = ic.readFloat("ref_distance", 10);
    }

    @Override
    public PointAudioSource clone(){
        return (PointAudioSource) super.clone();
    }

    @Override
    public String toString(){
        String parentToString = super.toString();
        parentToString = parentToString.substring(0, parentToString.length()-2);
        return parentToString + ", pos="+toString(position)+", vel="+toString(velocity) + "]";
    }

}
