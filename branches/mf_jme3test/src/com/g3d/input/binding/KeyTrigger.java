package com.g3d.input.binding;

import com.g3d.input.*;

public class KeyTrigger implements Trigger {

    private int keyCode;

    public KeyTrigger(int keyCode){
        if (keyCode < 0)
            throw new IllegalArgumentException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KeyTrigger other = (KeyTrigger) obj;
        if (this.keyCode != other.keyCode) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return keyCode;
    }

}
