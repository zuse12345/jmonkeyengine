package com.g3d.input.event;

import com.g3d.input.*;

public class KeyInputEvent extends InputEvent {

    private int keyCode;
    private char keyChar;
    private boolean pressed;
    private boolean down;

    public KeyInputEvent(int keyCode, char keyChar, boolean pressed, boolean down) {
        this.keyCode = keyCode;
        this.keyChar = keyChar;
        this.pressed = pressed;
        this.down = down;
    }

    public char getKeyChar() {
        return keyChar;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isReleased() {
        return !pressed;
    }

    public String toString(){
        String str = "Key(CODE="+keyCode;
        if (keyChar != '\0')
            str = str + ", CHAR=" + keyChar;
            
        if (down){
            return str + ", DOWN)";
        }else if (pressed){
            return str + ", PRESSED)";
        }else{
            return str + ", RELEASED)";
        }
    }
}
