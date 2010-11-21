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

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.renderer.RenderManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Nehon
 */
public class Cinematic extends AbstractCinematicEvent implements Savable, AppState, PlayStateListener {

    protected TimeLine timeLine = new TimeLine();
    protected float time = 0;
    private int lastFetchedKeyFrame = -1;
    private List<CinematicEvent> currentlyPlaying = new ArrayList<CinematicEvent>();
    private boolean requestedStop=false;

    @Override
    public void playEvent() {
        if(playState==PlayState.Paused){
            for (CinematicEvent ce : currentlyPlaying) {
               ce.play();
            }
        }
    }

    @Override
    public void stopEvent() {
        time = 0;
        lastFetchedKeyFrame = -1;
        requestedStop=true;
        for (Iterator<CinematicEvent> it = currentlyPlaying.iterator(); it.hasNext();) {
            CinematicEvent ce = it.next();
            ce.stop();
            it.remove();
        }
        requestedStop=false;
    }

    @Override
    public void pauseEvent() {
        for (CinematicEvent ce : currentlyPlaying) {
            ce.pause();
        }
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(speed);
        duration = initialDuration / speed;
        for (KeyFrame kf : timeLine.getAllKeyFrames()) {
            for (CinematicEvent ce : kf.getCinematicEvents()) {
                ce.setSpeed(speed);
            }
        }

    }

    public void initialize(AppStateManager stateManager, Application app) {
    }

    public boolean isInitialized() {
        return true;
    }

    public void setActive(boolean active) {
        if (active) {
            play();
        }
    }

    public boolean isActive() {
        return playState == PlayState.Playing;
    }

    public void stateAttached(AppStateManager stateManager) {
    }

    public void stateDetached(AppStateManager stateManager) {
        stop();
    }

    public void update(float tpf) {
        if (playState == PlayState.Playing) {
            time += tpf * speed;
            int keyFrameIndex = timeLine.getKeyFrameIndexFromTime(time);

            //iterate to make sure every key frame is triggered
            for (int i = lastFetchedKeyFrame + 1; i <= keyFrameIndex; i++) {
                KeyFrame keyFrame = timeLine.get(i);
                if (keyFrame != null) {
                    currentlyPlaying.addAll(keyFrame.trigger());
                }
            }
            lastFetchedKeyFrame = keyFrameIndex;
            if (time >= duration) {
                stop();
            }
        }
    }

    public KeyFrame addCinematicEvent(float timeStamp, CinematicEvent cinematicEvent) {
        KeyFrame keyFrame = timeLine.getKeyFrameAtTime(timeStamp);
        if (keyFrame == null) {
            keyFrame = new KeyFrame();
            timeLine.addKeyFrameAtTime(timeStamp, keyFrame);
        }
        keyFrame.cinematicEvents.add(cinematicEvent);
        cinematicEvent.addListener(this);
        return keyFrame;
    }

    public void render(RenderManager rm) {
    }

    public void postRender() {
    }

    public void cleanup() {
    }

    public void fitDuration() {
        KeyFrame kf = timeLine.getKeyFrameAtTime(timeLine.getLastKeyFrameIndex());
        float d = 0;
        for (CinematicEvent ce : kf.getCinematicEvents()) {
            if (d < (ce.getDuration() * ce.getSpeed())) {
                d = (ce.getDuration() * ce.getSpeed());
            }
        }
        initialDuration = d;
    }

    public void onPlayStateChange(CinematicEvent cinematicEvent) {
        if(cinematicEvent.getPlayState()==PlayState.Stoped && !requestedStop){
            currentlyPlaying.remove(cinematicEvent);
        }
    }
}
