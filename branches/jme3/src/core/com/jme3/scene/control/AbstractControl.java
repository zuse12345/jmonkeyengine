package com.jme3.scene.control;

import com.jme3.scene.control.Control;
import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
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

    /**
     * To be implemented in subclass.
     */
    protected abstract void controlRender(RenderManager rm, ViewPort vp);

    public void update(float tpf) {
        if (!enabled)
            return;

        controlUpdate(tpf);
    }

    public void render(RenderManager rm, ViewPort vp) {
        if (!enabled)
            return;

        controlRender(rm, vp);
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
