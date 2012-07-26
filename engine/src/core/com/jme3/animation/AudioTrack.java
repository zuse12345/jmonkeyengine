/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
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
package com.jme3.animation;

import com.jme3.audio.AudioNode;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.util.TempVars;
import java.io.IOException;

/**
 * AudioTrack is a track to add to an existing animation, to paly a sound during an animations
 * for example : gun shot, foot step, shout, etc...
 * 
 * usage is 
 * <pre>
 * AnimControl control model.getControl(AnimControl.class);
 * AudioTrack track = new AudioTrack(existionAudioNode, control.getAnim("TheAnim").getLength());
 * control.getAnim("TheAnim").addTrack(track);
 * </pre>
 * 
 * This is mostly intended for short sounds, playInstance will be called on the AudioNode at time 0 + startOffset. 
 * 
 *
 * @author Nehon
 */
public class AudioTrack implements Track {

    private AudioNode audio;
    private float startOffset = 0;
    private float length = 0;
    private boolean initialized = false;
    private boolean started = false;

    //Animation listener to stop the sound when the animation ends or is changed
    private class OnEndListener implements AnimEventListener {

        public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
            stop();
        }

        public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
            stop();
        }
    }

    /**
     * default constructor for serialization only
     */
    public AudioTrack() {
    }

    /**
     * Creates an AudioTrack
     * @param audio the AudioNode
     * @param length the length of the track (usually the length of the animation you want to add the track to)
     */
    public AudioTrack(AudioNode audio, float length) {
        this.audio = audio;
        this.length = length;
    }

    /**
     * Creates an AudioTrack
     * @param audio the AudioNode
     * @param length the length of the track (usually the length of the animation you want to add the track to)
     * @param startOffset the time in second when the sound will be played after the animation starts (default is 0)
     */
    public AudioTrack(AudioNode audio, float length, float startOffset) {
        this(audio, length);
        this.startOffset = startOffset;
    }

    /**
     * Internal use only
     * @see Track#setTime(float, float, com.jme3.animation.AnimControl, com.jme3.animation.AnimChannel, com.jme3.util.TempVars) 
     */
    public void setTime(float time, float weight, AnimControl control, AnimChannel channel, TempVars vars) {

        if (!initialized) {
            control.addListener(new OnEndListener());
            initialized = true;
        }
        if (!started && time >= startOffset) {
            started = true;
            audio.playInstance();
        }
    }

    //stops the sound
    private void stop() {
        audio.stop();
        started = false;
    }

    /**
     * Retruns the length of the track
     * @return length of the track
     */
    public float getLength() {
        return length;
    }

    /**
     * Clone this track
     * @return 
     */
    @Override
    public Track clone() {
        return new AudioTrack(audio, length, startOffset);
    }

    /**
     * Internal use only serialization
     * @param ex exporter
     * @throws IOException exception
     */
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write(audio, "audio", null);
        out.write(length, "length", 0);
        out.write(startOffset, "startOffset", 0);
    }

    /**
     * Internal use only serialization
     * @param im importer
     * @throws IOException Exception
     */
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        audio = (AudioNode) in.readSavable("audio", null);
        length = in.readFloat("length", length);
        startOffset = in.readFloat("startOffset", 0);
    }
}
