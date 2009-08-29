package com.g3d.app.state;

import com.g3d.renderer.Camera;

public interface CameraAppState extends AppService {
    public Camera getCamera();
}
