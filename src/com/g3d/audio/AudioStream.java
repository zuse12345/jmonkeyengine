package com.g3d.audio;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioStream extends AudioData implements Closeable{

    protected InputStream in;
    private boolean open = false;
    private int[] bufIds = new int[2];

    public AudioStream(){
    }

    public void updateData(InputStream in){
        if (id != -1 || this.in != null)
            throw new IllegalStateException("Data already set!");

        this.in = in;
        open = true;
    }

    public int readSamples(byte[] buf, int offset, int length){
        try{
            return in.read(buf, offset, length);
        }catch (IOException ex){
            return -1;
        }
    }

    public int readSamples(byte[] buf){
        return readSamples(buf, 0, buf.length);
    }

//    public int getId(int index){
//        return bufIds[index];
//    }
//
//    @Override
//    public int getId(){
//        return getId(0);
//    }
//
//    public void setId(int index, int id){
//        bufIds[index] = id;
//    }
//
//    @Override
//    public void setId(int id){
//        setId(0,id);
//    }

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

    public boolean isOpen(){
        return open;
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
