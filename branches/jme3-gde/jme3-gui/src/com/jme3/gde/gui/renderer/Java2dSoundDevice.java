/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.gui.renderer;

import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.spi.sound.SoundDevice;
import de.lessvoid.nifty.spi.sound.SoundHandle;

/**
 *
 * @author normenhansen
 */
public class Java2dSoundDevice implements SoundDevice{

    public SoundHandle loadSound(SoundSystem soundSystem, String filename) {
        return new Java2dSoundHandle();
    }

    public SoundHandle loadMusic(SoundSystem soundSystem, String filename) {
        return new Java2dSoundHandle();
    }

    public void update(int delta) {
    }

}
