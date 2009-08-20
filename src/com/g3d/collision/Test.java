package com.g3d.collision;

import com.g3d.math.Vector3f;

public class Test {

    public static void main(String[] args){
        Vector3f[] points = new Vector3f[6];
        points[0] = new Vector3f(2,3,0);
        points[1] = new Vector3f(5,4,0);
        points[2] = new Vector3f(9,6,0);
        points[3] = new Vector3f(4,7,0);
        points[4] = new Vector3f(8,1,0);
        points[5] = new Vector3f(7,2,0);

        KDNode root = KDNode.createKDTree(points);
        System.out.println(root);
    }

}
