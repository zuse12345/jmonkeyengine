package com.jme3.input.event;

import com.jme3.input.*;

public class MouseButtonEvent extends InputEvent {

    int btnIndex;
    boolean pressed;
    boolean down;

    public MouseButtonEvent(int btnIndex, boolean pressed, boolean down) {
        this.btnIndex = btnIndex;
        this.pressed = pressed;
        this.down = down;
    }

    public int getButtonIndex() {
        return btnIndex;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isReleased() {
        return !pressed;
    }
    
    public String toString(){
        String str = "MouseButton(BTN="+btnIndex;
        if (down){
            return str + ", DOWN)";
        }else if (pressed){
            return str + ", PRESSED)";
        }else{
            return str + ", RELEASED)";
        }
    }

}
