package com.g3d.audio;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.Savable;
import java.io.IOException;

public class Filter implements Savable {

    protected int id = -1;
    protected boolean updateNeeded = true;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void clearUpdateNeeded(){
        this.updateNeeded = false;
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    public void write(G3DExporter ex) throws IOException {
        // nothing to save
    }

    public void read(G3DImporter im) throws IOException {
        // nothing to read
    }

}
