package com.g3d.scene;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import java.io.IOException;

/**
 * An abstract implementation of the Control interface.
 *
 * @author Kirill Vainer
 */
public abstract class AbstractControl implements Control {

    protected boolean enabled = true;
    protected Spatial spatial;

    public AbstractControl(Spatial spatial){
        this.spatial = spatial;
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * To be implemented in subclass.
     */
    protected abstract void controlUpdate(float tpf);

    public void update(float tpf) {
        if (!enabled)
            return;

        controlUpdate(tpf);
    }

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(enabled, "enabled", true);
        oc.write(spatial, "spatial", null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        enabled = ic.readBoolean("enabled", true);
        spatial = (Spatial) ic.readSavable("spatial", null);
    }

}
