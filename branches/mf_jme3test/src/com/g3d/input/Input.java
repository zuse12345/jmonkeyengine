package com.g3d.input;

public interface Input {

    public void initialize();
    public void update();
    public void destroy();
    
    public boolean isInitialized();

    public void setInputListener(RawInputListener listener);
}
