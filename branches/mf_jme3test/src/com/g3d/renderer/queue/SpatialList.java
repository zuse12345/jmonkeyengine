package com.g3d.renderer.queue;

import com.g3d.scene.Spatial;
import com.g3d.util.SortUtil;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This class is a special function list of Spatial objects for render
 * queueing.
 *
 * @author Jack Lindamood
 * @author Three Rings - better sorting alg.
 */
public class SpatialList {

    Spatial[] list, tlist;

    int listSize;

    private static final int DEFAULT_SIZE = 32;

    private Comparator<Spatial> c;

    SpatialList(Comparator<Spatial> c) {
        listSize = 0;
        list = new Spatial[DEFAULT_SIZE];
        this.c = c;
    }

    /**
     * Adds a spatial to the list. List size is doubled if there is no room.
     *
     * @param s
     *            The spatial to add.
     */
    void add(Spatial s) {
        if (listSize == list.length) {
            Spatial[] temp = new Spatial[listSize * 2];
            System.arraycopy(list, 0, temp, 0, listSize);
            list = temp;
        }
        list[listSize++] = s;
    }

    /**
     * Resets list size to 0.
     */
    void clear() {
        for (int i = 0; i < listSize; i++)
            list[i] = null;
        if (tlist != null)
            Arrays.fill(tlist, null);
        listSize = 0;
    }

    /**
     * Sorts the elements in the list acording to their Comparator.
     */
    void sort() {
        if (listSize > 1) {
            // resize or populate our temporary array as necessary
            if (tlist == null || tlist.length != list.length) {
                tlist = list.clone();
            } else {
                System.arraycopy(list, 0, tlist, 0, list.length);
            }
            // now merge sort tlist into list
            SortUtil.msort(tlist, list, 0, listSize, c);
        }
    }
}