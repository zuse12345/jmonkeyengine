package com.g3d.shader;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import java.io.IOException;

public class ShaderVariable implements Savable {

    // if -2, location not known
    // if -1, not defined in shader
    // if >= 0, uniform defined and available.
    protected int location = -2;

    /**
     * Name of the uniform as was declared in the shader.
     * E.g name = "g_WorldMatrix" if the decleration was
     * "uniform mat4 g_WorldMatrix;".
     */
    protected String name = null;

    /**
     * True if the shader value was changed.
     */
    protected boolean updateNeeded = true;;

    public void write(G3DExporter ex) throws IOException{
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", null);
    }

    public void read(G3DImporter im) throws IOException{
        InputCapsule ic = im.getCapsule(this);
        name = ic.readString("name", null);
    }

    public void setLocation(int location){
        this.location = location;
    }

    public int getLocation(){
        return location;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
