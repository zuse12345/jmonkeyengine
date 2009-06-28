/*
 * @(#)$Id$
 *
 * Copyright (c) 2009, Blaine Simpson and the jMonkeyEngine Dev Team.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.net.MalformedURLException;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import com.jme.animation.AnimationController;
import com.jme.animation.BoneAnimation;

/**
 * Sample SimpleGame class which extends the behavior of XmlWorld to execute
 * the first animation of a Spatial named 'ArmatureSuperBone.
 * This Spatial will be created automatically by the * Blender ==> jME Exporter
 * if your Blender scene has an Armature with Blender's default name of
 * 'Armature' (and you select it for export).
 *
 * @author Blaine Simpson (blaine dot simpson at admc dot com)
 * @see #main(String[])
 */
public class XmlAnimator extends XmlWorld {
    /**
     * Instantiate a jME game world, loading the specified jME XML models
     * into the scene, then executing the first animation present.
     *
     * @param args
     *     <CODE><PRE>
     *     Syntax:  java... XmlAnimator [-r] file:model1-jme.xml...
     *     </PRE><CODE>
     *     where "-r" means to display the settings widget.
     *
     */
    static public void main(String[] args) throws MalformedURLException {
        XmlWorld.parseAndRun(new XmlAnimator(), args);
    }

    /**
     * Loads and runs the animation.
     */
    protected void simpleInitGame() {
        super.simpleInitGame();
        AnimationController ac = getAnimationController();
        System.out.println("Available animations:");
        for (BoneAnimation anim : ac.getAnimations())
            System.out.println("    " + anim.getName());
        ac.setActiveAnimation(0);
        System.err.println("Executing animation '"
                + ac.getActiveAnimation().getName() + "'");
    }

    /**
     * Gets the AnimationController of the Spatial named 'ArmatureSuperBone'"
     */
    public AnimationController getAnimationController() {
        Spatial armature = rootNode.getChild("ArmatureSuperBone");
        if (armature == null)
            throw new IllegalStateException("Sorry.  Program assumes "
                    + "you have a node named 'ArmatureSuperBone'");
        if (armature.getControllerCount() != 1)
            throw new IllegalStateException(
                    "Armature should have 1 controller, but has "
                    + armature.getControllerCount());
        Controller controller = armature.getController(0);
        if (!(controller instanceof AnimationController))
            throw new IllegalStateException(
                    "Controller is of unexpected type: "
                    + controller.getClass().getName());
        return (AnimationController) controller;
    }
}
