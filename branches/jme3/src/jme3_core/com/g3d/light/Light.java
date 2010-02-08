package com.g3d.light;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.math.ColorRGBA;
import com.g3d.scene.Spatial;
import java.io.IOException;

/**
 * Abstract class for representing a light source.
 */
public abstract class Light implements Savable, Cloneable {

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
    protected transient float lastDistance = -1;

    /**
     * @return The color of the light.
     */
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

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(color, "color", null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        color = (ColorRGBA) ic.readSavable("color", null);
    }

    public abstract void computeLastDistance(Spatial owner);
    public abstract Type getType();

}
