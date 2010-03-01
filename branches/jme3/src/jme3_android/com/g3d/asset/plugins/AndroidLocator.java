package com.g3d.asset.plugins;

import android.content.res.AssetManager;
import android.content.res.Resources;
import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetKey;
import com.g3d.asset.AssetLocator;
import com.g3d.system.G3DSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class AndroidLocator implements AssetLocator {

    private static final Logger logger = Logger.getLogger(AndroidLocator.class.getName());
    private Resources resources;
    private AssetManager androidManager;

    private class AndroidAssetInfo extends AssetInfo {

        private final InputStream in;

        public AndroidAssetInfo(com.g3d.asset.AssetManager manager, AssetKey key,
                InputStream in){
            super(manager, key);
            this.in = in;
        }

        @Override
        public InputStream openStream() {
            return in;
        }
    }


    public AndroidLocator(){
        resources = G3DSystem.getResources();
        androidManager = resources.getAssets();
    }
    
    public void setRootPath(String rootPath) {
    }

    public AssetInfo locate(com.g3d.asset.AssetManager manager, AssetKey key) {
        InputStream in = null;
        try {
            in = androidManager.open(key.getName());
            if (in == null)
                return null;

            return new AndroidAssetInfo(manager, key, in);
        } catch (IOException ex) {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ex1) {
                }
        }
        return null;
    }

}
