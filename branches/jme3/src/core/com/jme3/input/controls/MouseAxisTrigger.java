package com.jme3.input.controls;

import com.jme3.input.MouseInput;

public class MouseAxisTrigger implements Trigger {

    private int mouseAxis;
    private boolean negative;

    public MouseAxisTrigger(int mouseAxis, boolean negative){
        if (mouseAxis < 0 || mouseAxis > 2)
            throw new IllegalArgumentException("Mouse Axis must be between 0 and 2");

        this.mouseAxis = mouseAxis;
        this.negative = negative;
    }

    public int getMouseAxis(){
        return mouseAxis;
    }

    public String getName() {
        switch (mouseAxis){
            case MouseInput.AXIS_X: return "Mouse X Axis";
            case MouseInput.AXIS_Y: return "Mouse Y Axis";
            case MouseInput.AXIS_WHEEL: return "Mouse Wheel";
            default: throw new AssertionError();
        }
    }

    @Override
    public int hashCode(){
        return Controls.mouseAxisHash(mouseAxis, negative);
    }
}
