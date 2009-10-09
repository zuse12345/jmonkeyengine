package com.g3d.physics;

import com.g3d.bounding.BoundingSphere;
import com.g3d.math.Vector3f;

/**
 * <code>DynamicCollider</code> provides an abstract class for dynamic colliders.
 * 
 * @author Lucas Goraieb
 */
public abstract class DynamicCollider extends Collider {

    protected PhysicState physicState = new PhysicState();
    protected BoundingSphere worldSpaceBounds = new BoundingSphere();

    /**
     * Returns the physic material of this collider
     */
    public PhysicMaterial getPhysicMaterial() {
        return physicState.getMaterial();
    }

    /**
     * Returns the physic state of this collider
     */
    public PhysicState getPhysicState() {
        return physicState;
    }

    protected void updateSpatial() {
        if (spatial != null){
            spatial.setLocalTranslation(location);
        }
    }

    protected void onCollidedWith(Collider collider, Vector3f position, Vector3f normal, Vector3f velocity) {
        physicState.addCollision(collider, normal, velocity);
        for (CollisionListener cl : listeners){
            cl.collided(this, collider, position, normal, velocity);
        }
    }

    protected abstract float intersectTriangle(Vector3f center, Vector3f velocity,
            Vector3f triangleOne, Vector3f triangleTwo, Vector3f triangleThree,
            Vector3f intersectionPoint, Vector3f intersectionNormal);

    protected abstract float intersectEllipse(Vector3f center, Vector3f velocity,
            Vector3f ellipseLocation, Vector3f ellipseDimension,
            Vector3f intersectionPoint, Vector3f intersectionNormal);

    protected abstract void updateWorldBound(Vector3f velocity);

    protected abstract void updateLocation(Vector3f newLocation);
}
