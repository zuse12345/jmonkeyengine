package com.g3d.audio;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class AudioStream extends AudioData implements Closeable{

    private InputStream in;
    private boolean open = false;

    public AudioStream(){
    }

    public void updateData(InputStream in){
        if (id != -1 || in != null)
            throw new IllegalStateException("Data already set!");

        this.in = in;
        open = true;
    }

    @Override
    public DataType getDataType() {
        return DataType.Stream;
    }

    @Override
    public void resetObject() {
        id = -1;
        setUpdateNeeded();
    }

    @Override
    public void deleteObject(AudioRenderer r) {
        r.deleteAudioData(this);
    }

    public void close() throws IOException {
        if (in != null && open){
            in.close();
            open = false;
        }else{
            throw new IOException("AudioStream is already closed!");
        }
    }

}
