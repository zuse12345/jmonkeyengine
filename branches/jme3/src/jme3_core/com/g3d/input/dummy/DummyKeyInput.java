package com.g3d.input.dummy;

import com.g3d.input.KeyInput;

public class DummyKeyInput extends DummyInput implements KeyInput {

    public int getKeyCount() {
        if (!inited)
            throw new IllegalStateException("Input not initialized.");

        return 0;
    }

}
