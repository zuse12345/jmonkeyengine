package com.g3d.collision;

import com.g3d.bounding.BoundingVolume;
import com.g3d.export.Savable;
import com.g3d.math.Ray;

public interface CollisionTree extends Savable {

    public void construct();
    public void intersectWhere(Ray r, float rayLength, TrianglePickResults results);
    public void intersectWhere(BoundingVolume bv, TrianglePickResults results);

}
