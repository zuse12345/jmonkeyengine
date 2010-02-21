/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.scene;

import com.g3d.export.Savable;

/**
 * An interface for scene-graph controls. 
 *
 * @author Kirill Vainer
 */
public interface Control extends Savable {

    /**
     * @param spatial the spatial to be controlled. This should not be called
     * from user code.
     */
    public void setSpatial(Spatial spatial);

    /**
     * @param enabled Enable or disable the control. If disabled, update()
     * should do nothing.
     */
    public void setEnabled(boolean enabled);

    /**
     * @return True if enabled, false otherwise.
     * @see Control#setEnabled(boolean)
     */
    public boolean isEnabled();

    /**
     * Updates the control. This should not be called from user code.
     * @param tpf Time per frame.
     */
    public void update(float tpf);
}
