package com.jme3.asset.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JARLocator implements AssetLocator {

    private ZipFile zipfile;
    private static final Logger logger = Logger.getLogger(JARLocator.class.getName());

    private class JarAssetInfo extends AssetInfo {

        private final ZipEntry entry;

        public JarAssetInfo(AssetManager manager, AssetKey key, ZipEntry entry){
            super(manager, key);
            this.entry = entry;
        }

        public InputStream openStream(){
            try{
                return zipfile.getInputStream(entry);
            }catch (IOException ex){
                logger.log(Level.WARNING, "Failed to load zip entry: "+entry, ex);
            }
            return null;
        }
    }

    public void setRootPath(String rootPath) {
        try{
            zipfile = new ZipFile(new File(rootPath), ZipFile.OPEN_READ);
        }catch (IOException ex){
        }
    }

    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        ZipEntry entry = zipfile.getEntry(name);
        if (entry == null)
            return null;
        
        return new JarAssetInfo(manager, key, entry);
    }

}
