package com.g3d.asset.pack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public abstract class J3P {

    public static enum Access {
        Copy,
        Parse,
        Stream
    }

    public static final J3P openURL(URL url) throws IOException{
        if (url.getProtocol().equals("file")){
            File f;
            try{
                f = new File(url.toURI());
            }catch (URISyntaxException e){
                f = new File(url.getPath());
            }
            if (f.exists() && f.isFile()){
                J3PFile j3p = new J3PFile();
                j3p.open(f);
                return j3p;
            }
            return null;
        }else if (url.getProtocol().equals("http")){
            J3PHttpFile j3p = new J3PHttpFile();
            j3p.open(url);
            return j3p;
        }else{
            throw new IOException("Unsupported protocol: "+url.getProtocol());
        }
    }

    public abstract Iterable<Integer> getEntryHashes();
    public abstract int getEntrySize(String name);
    public abstract int getEntrySize(int hash);

    public abstract ReadableByteChannel openChannel(String name, Access access);
    public abstract ReadableByteChannel openChannel(int hash, Access access);

    public abstract ByteBuffer openBuffer(String name, Access access);
    public abstract ByteBuffer openBuffer(int hash, Access access);

    public abstract InputStream openStream(String name, Access access);
    public abstract InputStream openStream(int hash, Access access);

    public abstract void close() throws IOException;
}
