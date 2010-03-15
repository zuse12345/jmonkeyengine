package com.jme3.asset;

import com.jme3.export.G3DExporter;
import com.jme3.export.G3DImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import java.io.IOException;

/**
 * <code>AudioKey</code> is extending AssetKey by holding stream flag.
 *
 * @author Kirill
 */
public class AudioKey extends AssetKey {

    private boolean stream;

    /**
     * Create a new AudioKey
     *
     * @param name Name of the asset
     * @param stream If true, the audio will be streamed from harddrive,
     * otherwise it will be buffered entirely and then played.
     */
    public AudioKey(String name, boolean stream){
        super(name);
        this.stream = stream;
    }

    public AudioKey(String name){
        super(name);
        this.stream = false;
    }

    public AudioKey(){
    }

    public boolean isStream() {
        return stream;
    }

    public boolean shouldCache(){
        return !stream;
    }

    @Override
    public void write(G3DExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(stream, "do_stream", false);
    }

    @Override
    public void read(G3DImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        stream = ic.readBoolean("do_stream", false);
    }

}
