package com.g3d.input;

/**
 * A specific API for interfacing with the keyboard.
 */
public interface KeyInput extends Input {

    /**
     * @return The number of keys the connected keyboard has.
     */
    public int getKeyCount();
}
