package com.g3d.collision;

import com.g3d.math.Triangle;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import java.util.ArrayList;
import java.util.Collections;

public class TrianglePickResults {

    private final ArrayList<PickData> results = new ArrayList<PickData>();

    public static class PickData implements Comparable<PickData> {

        Geometry geometry;
        int index;
        float distance;

        public PickData(Geometry geometry, int index, float distance) {
            this.index = index;
            this.geometry = geometry;
            this.distance = distance;
        }

        public float getDistance() {
            return distance;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public int getIndex() {
            return index;
        }

        public Triangle getTriangle(Triangle store){
            if (store == null)
                store = new Triangle();

            Mesh m = geometry.getMesh();
            m.getTriangle(index, store);
            store.calculateCenter();
            store.calculateNormal();
            return store;
        }
        
        public int compareTo(PickData o) {
            if (distance > o.distance)
                return -1;
            else if (distance < o.distance)
                return 1;
            else
                return 0;
        }
    }

    public TrianglePickResults(){
    }

    public void clear(){
        results.clear();
    }

    public void addPick(Geometry g, int index, float distance){
        results.add(new PickData(g, index, distance));
    }

    public void finish(){
        Collections.sort(results);
    }

    public int size(){
        return results.size();
    }

    public PickData getClosestPick(){
        if (size() == 0)
            return null;

        return results.get(size()-1);
    }

    public PickData getPick(int index){
        return results.get(index);
    }

}
