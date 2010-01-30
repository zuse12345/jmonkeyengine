package com.g3d.scene;

import java.nio.ShortBuffer;

public class LodData {

    private ShortBuffer[] levels;
    private float[] distances;

    public ShortBuffer getLevelForDist(float dist){
        for (int i = 0; i < levels.length; i++){
            if (dist < distances[i]){
                return levels[i];
            }
        }
        return null;
    }

}