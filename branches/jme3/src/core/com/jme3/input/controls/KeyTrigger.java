package com.jme3.input.controls;

public class KeyTrigger implements Trigger {

    private final int keyCode;

    public KeyTrigger(int keyCode){
        this.keyCode = keyCode;
    }

    public String getName() {
        return "KeyCode " + keyCode;
    }

    public int getKeyCode(){
        return keyCode;
    }

    @Override
    public int hashCode(){
        return Controls.keyHash(keyCode);
    }

}
