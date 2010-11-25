/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.cinematic;

import de.lessvoid.nifty.Nifty;

/**
 *
 * @author Nehon
 */
public class GuiTrack extends AbstractCinematicEvent {

    protected String screen;
    protected Nifty nifty;

    public GuiTrack() {
    }

    public GuiTrack(Nifty nifty, String screen,float initialDuration) {
        this.nifty = nifty;
        this.screen = screen;
        setInitalDuration(initialDuration);
    }

    @Override
    public void onPlay() {
        nifty.gotoScreen(screen);
    }

    @Override
    public void onStop() {
        nifty.gotoScreen("");
    }

    @Override
    public void onPause() {
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    @Override
    public void onUpdate(float tpf) {
    
    }
}
