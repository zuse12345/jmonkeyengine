/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.bullet;

import com.bulletphysics.dynamics.InternalTickCallback;

/**
 *
 * @author normenhansen
 */
public interface PhysicsTickListener {

    public void physicsTick(PhysicsSpace space, float f);

}
