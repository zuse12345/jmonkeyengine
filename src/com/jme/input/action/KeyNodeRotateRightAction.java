/*
 * Copyright (c) 2003-2004, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.jme.input.action;

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 * <code>KeyNodeRotateRightAction</code> rotates a node to the right. The axis
 * of rotation is dependant on the setting of the lock axis. If no lock axis
 * is set, the node rotates about it's y-axis. This will allow the node to
 * roll. However, to prevent rolling, setting the lock axis to the world's
 * y-axis (or any desired axis for that matter), will cause the node to
 * rotate about the world. The locking of the axis is particularly useful for
 * control schemes similar to first person shooters.
 * @author Mark Powell
 * @version $Id: KeyNodeRotateRightAction.java,v 1.7 2004-04-23 16:39:11 renanse Exp $
 */
public class KeyNodeRotateRightAction extends AbstractInputAction {
    private Matrix3f incr;
    private Spatial node;
    private Vector3f lockAxis;

    /**
     * Constructor instantiates a new <code>KeyNodeRotateRightAction</code>
     * object using the node and speed parameters for it's attributes.
     * @param node the node that will be affected by this action.
     * @param speed the speed at which the node can move.
     */
    public KeyNodeRotateRightAction(Spatial node, float speed) {
        incr = new Matrix3f();
        this.node = node;
        this.speed = speed;
    }

    /**
     *
     * <code>setLockAxis</code> allows a certain axis to be locked, meaning
     * the camera will always be within the plane of the locked axis. For
     * example, if the node is a first person camera, the user might lock
     * the node's up vector. This will keep the node vertical with the
     * ground.
     * @param lockAxis the axis to lock.
     */
    public void setLockAxis(Vector3f lockAxis) {
        this.lockAxis = lockAxis;
    }

    /**
     * <code>performAction</code> rotates the camera about it's up vector or
     * lock axis at a speed of movement speed * time. Where time is
     * the time between frames and 1 corresponds to 1 second.
     * @see com.jme.input.action.InputAction#performAction(float)
     */
    public void performAction(float time) {
        incr.loadIdentity();
        if(lockAxis == null) {
            incr.fromAxisAngle(node.getLocalRotation().getRotationColumn(1), -speed * time);
        } else {
            incr.fromAxisAngle(lockAxis, -speed * time);
        }
        node.getLocalRotation().fromRotationMatrix(incr.mult(node.getLocalRotation().toRotationMatrix()));
        node.getLocalRotation().normalize();
        node.updateWorldData(time);
    }
}
