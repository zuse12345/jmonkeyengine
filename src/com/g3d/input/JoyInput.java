package com.g3d.input;

public interface JoyInput extends Input {
    public int getJoyCount();
    public String getJoyName(int joyIndex);
    public int getAxesCount(int joyIndex);
    public int getButtonCount(int joyIndex);
}
