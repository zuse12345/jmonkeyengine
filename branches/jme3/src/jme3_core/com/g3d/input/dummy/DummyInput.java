package com.g3d.input.dummy;

import com.g3d.input.Input;
import com.g3d.input.MouseInput;
import com.g3d.input.RawInputListener;

public class DummyInput implements Input {

    protected boolean inited = false;

    public void initialize() {
        if (inited)
            throw new IllegalStateException("Input already initialized.");

        inited = true;
    }

    public void update() {
        if (!inited)
            throw new IllegalStateException("Input not initialized.");
    }

    public void destroy() {
        if (!inited)
            throw new IllegalStateException("Input not initialized.");

        inited = false;
    }

    public boolean isInitialized() {
        return inited;
    }

    public void setInputListener(RawInputListener listener) {
    }

}
