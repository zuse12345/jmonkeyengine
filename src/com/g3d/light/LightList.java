package com.g3d.light;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import com.g3d.export.Savable;
import com.g3d.scene.Spatial;
import com.g3d.util.SortUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LightList implements Iterable<Light>, Savable, Cloneable {

    Light[] list, tlist;
    float[] distToOwner;
    int listSize;
    Spatial owner;

    private static final int DEFAULT_SIZE = 32;

    private static final Comparator<Light> c = new Comparator<Light>() {
        /**
         * This assumes lastDistance have been computed in a previous step.
         */
        public int compare(Light l1, Light l2) {
            if (l1.lastDistance < l2.lastDistance)
                return -1;
            else if (l1.lastDistance > l2.lastDistance)
                return 1;
            else
                return 0;
        }
    };

    /**
     * Default constructor for serialization. Do not use
     */
    public LightList(){
    }

    public LightList(Spatial owner) {
        listSize = 0;
        list = new Light[DEFAULT_SIZE];
        distToOwner = new float[DEFAULT_SIZE];
        Arrays.fill(distToOwner, Float.NEGATIVE_INFINITY);
        this.owner = owner;
    }

    /**
     * Adds a light to the list. List size is doubled if there is no room.
     *
     * @param l
     *            The light to add.
     */
    public void add(Light l) {
        if (listSize == list.length) {
            Light[] temp = new Light[listSize * 2];
            float[] temp2 = new float[listSize * 2];
            System.arraycopy(list, 0, temp, 0, listSize);
            System.arraycopy(distToOwner, 0, temp2, 0, listSize);
            list = temp;
            distToOwner = temp2;
        }
        list[listSize++] = l;
        distToOwner[listSize] = Float.NEGATIVE_INFINITY;
    }

    /**
     * @return The size of the list.
     */
    public int size(){
        return listSize;
    }

    /**
     * @return the light at the given index.
     * @throws IndexOutOfBoundsException If the given index is outside bounds.
     */
    public Light get(int num){
        if (num > listSize || num < 0)
            throw new IndexOutOfBoundsException();

        return list[num];
    }

    /**
     * Resets list size to 0.
     */
    public void clear() {
        if (listSize == 0)
            return;

        for (int i = 0; i < listSize; i++)
            list[i] = null;

        if (tlist != null)
            Arrays.fill(tlist, null);

        listSize = 0;
    }

    /**
     * Sorts the elements in the list acording to their Comparator.
     * There are two reasons why lights should be resorted. 
     * First, if the lights have moved, that means their distance to 
     * the spatial changed. 
     * Second, if the spatial itself moved, it means the distance from it to 
     * the individual lights might have changed.
     * 
     *
     * @param transformChanged Whether the spatial's transform has changed
     */
    public void sort(boolean transformChanged) {
        if (listSize > 1) {
            // resize or populate our temporary array as necessary
            if (tlist == null || tlist.length != list.length) {
                tlist = list.clone();
            } else {
                System.arraycopy(list, 0, tlist, 0, list.length);
            }

            if (transformChanged){
                // check distance of each light
                for (int i = 0; i < listSize; i++){
                    list[i].computeLastDistance(owner);
                }
            }

            // now merge sort tlist into list
            SortUtil.msort(tlist, list, 0, listSize, c);
        }
    }

    /**
     * Updates a "world-space" light list, using the spatial's local-space
     * light list and its parent's world-space light list.
     *
     * @param local
     * @param parent
     */
    public void update(LightList local, LightList parent){
        // clear the list as it will be reconstructed
        // using the arguments
        clear();

        // add the lights from the local list
        for (int i = 0; i < local.listSize; i++){
            list[i] = local.list[i];
            distToOwner[i] = Float.NEGATIVE_INFINITY;
        }

        // if the spatial has a parent node, add the lights
        // from the parent list as well
        if (parent != null){
            for (int i = 0; i < parent.listSize; i++){
                int p = i + local.listSize;
                list[p] = parent.list[i];
                distToOwner[p] = Float.NEGATIVE_INFINITY;
            }
            listSize = local.listSize + parent.listSize;
        }else{
            listSize = local.listSize;
        }
    }

    public Iterator<Light> iterator() {
        return new Iterator<Light>(){
            LightList l;
            int index = 0;

            public boolean hasNext() {
                return index <= l.listSize;
            }
            public Light next() {
                return l.list[index++];
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public LightList clone(){
        try{
            return (LightList) super.clone();
        }catch (CloneNotSupportedException ex){
            throw new AssertionError();
        }
    }

    public void write(G3DExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(owner, "owner", null);

        List<Light> lights = new ArrayList<Light>();
        for (int i = 0; i < listSize; i++){
            lights.add(list[i]);
        }
        oc.writeSavableList(lights, "lights", null);
    }

    public void read(G3DImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        owner = (Spatial) ic.readSavable("owner", null);

        list = new Light[DEFAULT_SIZE];
        distToOwner = new float[DEFAULT_SIZE];
        
        List<Light> lights = ic.readSavableList("lights", null);
        listSize = lights.size();
        for (int i = 0; i < listSize; i++){
            list[i] = lights.get(i);
        }
        
        Arrays.fill(distToOwner, Float.NEGATIVE_INFINITY);
    }

}
