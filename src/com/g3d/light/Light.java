package com.g3d.light;

import com.g3d.math.ColorRGBA;
import com.g3d.scene.Spatial;

public abstract class Light {

    public static enum Type {

        Directional(0),
        Point(1),
        Spot(2);

        private int typeId;

        Type(int type){
            this.typeId = type;
        }

        public int getId(){
            return typeId;
        }
    }

    protected ColorRGBA color = new ColorRGBA(1f,1f,1f,1f);
    
    /**
     * Used in LightList for caching the distance 
     * to the owner spatial. Should be reset after the sorting.
     */
    protected float lastDistance = -1;

    public ColorRGBA getColor() {
        return color;
    }

    public void setLastDistance(float lastDistance){
        this.lastDistance = lastDistance;
    }

    public float getLastDistance(){
        return lastDistance;
    }

    public void setColor(ColorRGBA color){
        this.color.set(color);
    }

    public abstract void computeLastDistance(Spatial owner);
    public abstract Type getType();

}
