package com.g3d.app.state;

import com.g3d.asset.AssetManager;

/**
 * <code>AssetAppState</code> provides a means to access the
 * AssetManager. This allows loading of assets like textures, models, etc.
 * through other AppStates.
 */
public interface AssetAppState extends AppService {

    /**
     * @return The asset manager
     */
    public AssetManager getAssetManager();
}
