package com.jme3.input.controls;

public class MouseButtonTrigger implements Trigger {

    private final int mouseButton;

    public MouseButtonTrigger(int mouseButton) {
        if  (mouseButton < 0)
            throw new IllegalArgumentException("Mouse Button cannot be negative");

        this.mouseButton = mouseButton;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public String getName() {
        return "Mouse Button " + mouseButton;
    }

    @Override
    public int hashCode(){
        return Controls.mouseButtonHash(mouseButton);
    }

}
