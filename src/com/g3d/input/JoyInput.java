package com.g3d.input;

/**
 * A specific API for interfacing with joysticks or gaming controllers.
 */
public interface JoyInput extends Input {

    /**
     * @return The number of joysticks connected to the system
     */
    public int getJoyCount();

    /**
     * @param joyIndex
     * @return The name of the joystick at the given index.
     */
    public String getJoyName(int joyIndex);

    /**
     * @param joyIndex
     * @return The number of axes that a joystick posses at the given index.
     */
    public int getAxesCount(int joyIndex);

    /**
     * @param joyIndex
     * @return The number of buttons that a joystick posses at the given index.
     */
    public int getButtonCount(int joyIndex);
}
