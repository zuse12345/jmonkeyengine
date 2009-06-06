/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.renderer.queue;

import com.g3d.scene.Spatial;
import java.util.Comparator;

public class NullComparator implements Comparator<Spatial> {
    public int compare(Spatial o1, Spatial o2) {
        return 0;
    }
}
