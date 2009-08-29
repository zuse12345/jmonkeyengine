package com.g3d.asset.plugins;

import com.g3d.asset.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ClasspathLocator implements AssetLocator {

    private String root = "/";

    private static class ClasspathAssetInfo extends AssetInfo {

        private URL url;

        public ClasspathAssetInfo(AssetManager manager, AssetKey key, URL url){
            super(manager, key);
            this.url = url;
        }

        @Override
        public InputStream openStream() {
            try{
                return url.openStream();
            }catch (IOException ex){
                return null; // failure..
            }
        }
    }

    public ClasspathLocator(){
    }

    public void setRootPath(String rootPath) {
        this.root = rootPath;
    }
    
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        URL url;
        String name = key.getName();
        if (name.startsWith(root)){
            url = ClasspathLocator.class.getResource(name);
        }else{
            url = ClasspathLocator.class.getResource(root + name);
        }
        return new ClasspathAssetInfo(manager, key, url);
    }


}
