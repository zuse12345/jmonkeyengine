package com.g3d.input.event;

public class JoyAxisEvent extends InputEvent {

    public static final int AXIS_X = 0x0;
    public static final int AXIS_Y = 0x1;
    public static final int AXIS_Z = 0x2;
    public static final int AXIS_Z_ROT = 0x3;
    public static final int POV_X = 0x4;
    public static final int POV_Y = 0x5;

    private int joyIdx;
    private int axisIdx;
    private int realAxisIdx;
    private float value;

    public JoyAxisEvent(int joyIdx, int axisIdx, int realAxisIdx, float value) {
        this.joyIdx = joyIdx;
        this.axisIdx = axisIdx;
        this.realAxisIdx = realAxisIdx;
        this.value = value;
    }

    public int getAxisIndex() {
        return axisIdx;
    }

    public int getJoyIndex() {
        return joyIdx;
    }

    public int getRealAxisIndex() {
        return realAxisIdx;
    }

    public float getValue() {
        return value;
    }



}
