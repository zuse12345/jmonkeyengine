/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.bullet.collision;

/**
 * @author normenhansen
 */
public interface CollisionGroupListener {

    /**
     * called when two physics objects of this group are about to collide
     * @param nodeA CollisionObject #1
     * @param nodeB CollisionObject #2
     * @return true if PhysicsNode should collide, false otherwise
     */
    public boolean collide(CollisionObject nodeA, CollisionObject nodeB);

}
