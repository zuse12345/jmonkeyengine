package com.g3d.app.state;

import com.g3d.scene.Node;

/**
 * The <code>SceneAppState</code> is responsible for managing
 * and updating the scene's root node.
 */
public interface SceneAppState extends AppService {

    /**
     * @return The scene's root node.
     */
    public Node getRootNode();
}
