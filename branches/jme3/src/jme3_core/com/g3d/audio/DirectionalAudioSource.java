package com.g3d.audio;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.math.Vector3f;
import java.io.IOException;

public class DirectionalAudioSource extends PointAudioSource {

    protected Vector3f direction = new Vector3f(0,0,1);
    protected float innerAngle = 360;
    protected float outerAngle = 360;

    @Override
    public DirectionalAudioSource clone(){
        return (DirectionalAudioSource) super.clone();
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public float getInnerAngle() {
        return innerAngle;
    }

    public void setInnerAngle(float innerAngle) {
        this.innerAngle = innerAngle;
    }

    public float getOuterAngle() {
        return outerAngle;
    }

    public void setOuterAngle(float outerAngle) {
        this.outerAngle = outerAngle;
    }

    public void write(G3DExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(direction, "direction", null);
        oc.write(innerAngle, "inner_angle", 360);
        oc.write(outerAngle, "outer_angle", 360);
    }
    public void read(G3DImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        direction = (Vector3f) ic.readSavable("direction", null);
        innerAngle = ic.readFloat("inner_angle", 360);
        outerAngle = ic.readFloat("outer_angle", 360);
    }
    
}
