package com.g3d.collision;

import com.g3d.math.Triangle;
import com.g3d.math.Vector3f;
import java.util.ArrayList;
import java.util.Collections;

public class TrianglePickResults {

    private final ArrayList<PickData> results = new ArrayList<PickData>();

    private static class PickData implements Comparable<PickData> {

        Triangle triangle;
        float distance;

        public PickData(Triangle t, float distance) {
            this.triangle = t;
            this.distance = distance;
        }

        public float getDistance() {
            return distance;
        }

        public Triangle getTriangle() {
            return triangle;
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

    public void addPick(Vector3f v0, Vector3f v1, Vector3f v2, float distance){
        results.add(new PickData(new Triangle(v0, v1, v2), distance));
    }

    public void finish(){
        Collections.sort(results);
    }

    public int size(){
        return results.size();
    }

    public float getClosestDistance(){
        if (size() == 0)
            return Float.POSITIVE_INFINITY;

        return results.get(results.size()-1).distance;
    }

    public Triangle getClosestTriangle(){
        if (size() == 0)
            return null;

        return results.get(results.size()-1).triangle;
    }

    public float getDistance(int index){
        return results.get(index).distance;
    }

    public Triangle getTriangle(int index){
        return results.get(index).triangle;
    }

}
