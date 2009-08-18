package com.g3d.res.plugins;

import com.g3d.audio.AudioBuffer;
import com.g3d.audio.AudioData;
import com.g3d.audio.AudioStream;
import com.g3d.res.ContentKey;
import com.g3d.res.ContentLoader;
import com.g3d.res.ContentManager;
import com.g3d.util.BufferUtils;
import com.g3d.util.LittleEndien;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class WAVLoader implements ContentLoader {

    private static final Logger logger = Logger.getLogger(WAVLoader.class.getName());

    // all these are in big endian
    private static final int i_RIFF = 0x46464952;
    private static final int i_WAVE = 0x45564157;
    private static final int i_fmt  = 0x20746D66 ;
    private static final int i_data = 0x61746164;

    private boolean readStream = false;

    private AudioBuffer audioBuffer;
    private AudioStream audioStream;
    private AudioData audioData;
    private int bytesPerSec;
    private int dataLength;

    private DataInput in;
    private InputStream stream;

    private void readFormatChunk(int size) throws IOException{
        // if other compressions are supported, size doesn't have to be 16
        if (size != 16)
            logger.warning("Expected size of format chunk to be 16");

        int compression = in.readShort();
        if (compression != 1)
            throw new IOException("WAV Loader only supports PCM wave files");

        int channels = in.readShort();
        int sampleRate = in.readInt();

        bytesPerSec = in.readInt(); // used to calculate duration

        int bytesPerSample = in.readShort();
        int bitsPerSample = in.readShort();

        if (bitsPerSample % 8 != 0 || bitsPerSample <= 0 || bitsPerSample > 32)
            throw new IOException("Only 8, 16, 24, or 32 bits per sample is supported");

        if ( (bitsPerSample / 8) * channels != bytesPerSample)
            throw new IOException("Invalid bytes per sample value");

        if (bytesPerSample * sampleRate != bytesPerSec)
            throw new IOException("Invalid bytes per second value");

        audioData.setupFormat(channels, bitsPerSample, sampleRate);

        int remaining = size - 16;
        if (remaining > 0)
            in.skipBytes(remaining);

//        if (compression != 1){
//            int extraLength = in.readShort();
//            if (extraLength % 2 != 0) // make it word-aligned if its not
//                extraLength ++;
//
//            in.skipBytes(extraLength);
//        }

        
    }

    private void readDataChunkForBuffer(int len) throws IOException{
        dataLength = len;
        ByteBuffer data = BufferUtils.createByteBuffer(dataLength);
        byte[] buf = new byte[512];
        int read = 0;
        while ( (read = stream.read(buf)) > 0){
            data.put(buf, 0, read);
        }
        data.flip();
        audioBuffer.updateData(data);
        stream.close();
    }

    public Object load(ContentManager owner, InputStream stream, String extension, ContentKey key) throws IOException {
        this.stream = stream;
        in = new LittleEndien(stream);

        int sig = in.readInt();
        if (sig != i_RIFF)
            throw new IOException("File is not a WAVE file");
        
        // skip size
        in.readInt();
        if (in.readInt() != i_WAVE)
            throw new IOException("WAVE File does not contain audio");

        String streamProp = owner.getProperty("AudioStreaming");
        readStream = streamProp != null && streamProp.equals("true");

        if (readStream){
            audioStream = new AudioStream();
            audioData = audioStream;
        }else{
            audioBuffer = new AudioBuffer();
            audioData = audioBuffer;
        }

        while (true){
            int type = in.readInt();
            int len = in.readInt();

            switch (type){
                case i_fmt:
                    readFormatChunk(len);
                    break;
                case i_data:
                    if (readStream){
                        audioStream.updateData(stream);
                    }else{
                        readDataChunkForBuffer(len);
                    }
                    return audioData;
                default:
                    int skipped = in.skipBytes(len);
                    if (skipped <= 0)
                        return null;
                    
                    break;
            }
        }
    }
}
