package com.g3d.app.state;

import com.g3d.renderer.Renderer;

public interface RenderAppState extends AppService {
    public Renderer getRenderer();
}
