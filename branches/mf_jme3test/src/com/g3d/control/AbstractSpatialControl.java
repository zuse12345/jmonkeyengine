package com.g3d.control;

import com.g3d.scene.Spatial;

public abstract class AbstractSpatialControl implements SpatialControl {

    protected final Spatial spatial;
    protected boolean enabled = true;

    public AbstractSpatialControl(Spatial obj){
        this.spatial = obj;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public Spatial getSpatial(){
        return spatial;
    }

    public abstract void update(float tpf);

}
