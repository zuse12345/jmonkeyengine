/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.cinematic;

import com.jme3.animation.AnimChannel;

/**
 *
 * @author Nehon
 */
public class AnimationTrack extends AbstractCinematicEvent {

    protected AnimChannel channel;
    protected String animationName;
    protected String stopAnimationName;
    protected float pauseTime;

    public AnimationTrack() {
    }

    public AnimationTrack(AnimChannel channel, String animationName, String stopAnimationName) {
        this.channel = channel;
        this.animationName = animationName;
        this.stopAnimationName = stopAnimationName;
    }

    @Override
     public void internalUpdate(float tpf) {
        if (playState == PlayState.Playing) {
            time += tpf * speed;
            onUpdate(tpf);
            if (time >= duration && duration!=-1) {
                stop();
            }
        }else if(playState == PlayState.Playing){
            onUpdate(tpf);
        }

    }

    @Override
    public void onPlay() {

        channel.setAnim(animationName);
        if (playState == PlayState.Paused) {
            channel.setTime(pauseTime);
        }
    }

    @Override
    public void onUpdate(float tpf) {
        if (playState == PlayState.Paused) {
            channel.setTime(pauseTime);
          
        }
    }

    @Override
    public void onStop() {
        channel.setAnim(stopAnimationName);


    }

    @Override
    public void onPause() {
        pauseTime = time;       
    }
}
