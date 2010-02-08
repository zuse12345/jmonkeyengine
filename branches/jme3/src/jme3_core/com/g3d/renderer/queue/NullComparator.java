/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.renderer.queue;

import com.g3d.renderer.Camera;
import com.g3d.scene.Geometry;

public class NullComparator implements GeometryComparator {
    public int compare(Geometry o1, Geometry o2) {
        return 0;
    }

    public void setCamera(Camera cam) {
    }
}
