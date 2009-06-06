package com.g3d.input;

/**
 * Abstract interface for an input device.
 * 
 * @see MouseInput
 * @see KeyInput
 * @see JoyInput
 */
public interface Input {

    /**
     * Initializes the native side to listen into events from the device.
     */
    public void initialize();

    /**
     * Queries the device for input. All events should be sent to the
     * RawInputListener set with setInputListener.
     *
     * @see #setInputListener(com.g3d.input.RawInputListener)
     */
    public void update();

    /**
     * Ceases listening to events from the device.
     */
    public void destroy();

    /**
     * @return True if the device has been initialized and not destroyed.
     * @see #initialize()
     * @see #destroy() 
     */
    public boolean isInitialized();

    public void setInputListener(RawInputListener listener);
}
