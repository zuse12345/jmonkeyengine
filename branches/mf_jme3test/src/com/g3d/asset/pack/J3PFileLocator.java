package com.g3d.asset.pack;

import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetKey;
import com.g3d.asset.AssetLocator;
import com.g3d.asset.AssetManager;
import com.g3d.asset.pack.J3P.Access;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class J3PFileLocator implements AssetLocator {

    private static final Logger logger = Logger.getLogger(J3PFileLocator.class.getName());

    private String rootPath;
    private File file;
    private J3P j3p;

    public void setRootPath(String rootPath) {
        if (rootPath.startsWith("http")){
            try{
                // this is a URL
                j3p = J3P.openURL(new URL(rootPath));
            }catch (IOException ex){
                logger.log(Level.SEVERE, "Failed to open online J3P: "+rootPath, ex);
            }
        }else{
            int exIdx = rootPath.indexOf("!");
            if (exIdx > 0){
                this.file = new File(rootPath.substring(0, exIdx));
                this.rootPath = rootPath.substring(exIdx + 1);
                if (!this.rootPath.endsWith("/"))
                    rootPath += "/";
            }else{
                this.file = new File(rootPath);
                this.rootPath = "";
            }
            J3PFile fileJ3P = new J3PFile();
            try{
                fileJ3P.open(file);
                j3p = fileJ3P;
            }catch (IOException ex){
                logger.log(Level.SEVERE, "Failed to open J3P File: "+rootPath, ex);
            }
        }
    }

    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        if (name.startsWith("/"))
            name = name.substring(1);

        final String finalName = rootPath + name;
        int size = j3p.getEntrySize(name);
        if (size <= 0)
            return null;
        
        AssetInfo info = new AssetInfo(manager, key) {
            @Override
            public InputStream openStream() {
                return j3p.openStream(finalName, Access.Parse);
//                ReadableByteChannel chan = j3p.openChannel(finalName, Access.Parse);
//                if (chan != null)
//                    return Channels.newInputStream(chan);
//                else
//                    return null;
            }
        };
        return info;
    }

}
