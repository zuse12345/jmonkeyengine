package com.g3d.asset;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
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
     * @param stream If true, the audio will be streamed from harddrive, other
     * wise it will be buffered entirely and then played.
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
