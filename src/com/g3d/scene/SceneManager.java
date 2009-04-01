package com.g3d.scene;

import com.g3d.renderer.Renderer;

public interface SceneManager {

    public void init(Renderer r);
    public void update(float tpf);
    public void render(Renderer r);

}
