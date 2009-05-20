package com.g3d.renderer.queue;

import com.g3d.scene.Spatial;
import java.lang.reflect.Array;
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

    private static final int DEFAULT_SIZE = 32;

    private Spatial[] spatials;
    private int size;
    private Comparator<Spatial> comparator;

    public SpatialList(Comparator<Spatial> comparator) {
        size = 0;
        spatials = new Spatial[DEFAULT_SIZE];
        this.comparator = comparator;
    }

    public int size(){
        return size;
    }

    public Spatial get(int index){
        return spatials[index];
    }

    /**
     * Adds a spatial to the list. List size is doubled if there is no room.
     *
     * @param s
     *            The spatial to add.
     */
    public void add(Spatial s) {
        if (size == spatials.length) {
            Spatial[] temp = new Spatial[size * 2];
            System.arraycopy(spatials, 0, temp, 0, size);
            spatials = temp; // original list replaced by double-size list
        }
        spatials[size++] = s;
    }

    /**
     * Resets list size to 0.
     */
    public void clear() {
        for (int i = 0; i < size; i++){
            spatials[i] = null;
        }

        size = 0;
    }

    /**
     * Sorts the elements in the list acording to their Comparator.
     */
    public void sort() {
        if (size > 1) {
            // sort the spatial list using the comparator
            Arrays.sort(spatials, comparator);
        }
    }
}