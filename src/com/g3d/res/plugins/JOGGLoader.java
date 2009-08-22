package com.g3d.res.plugins;

import com.g3d.audio.AudioBuffer;
import com.g3d.audio.AudioStream;
import com.g3d.res.ContentKey;
import com.g3d.res.ContentLoader;
import com.g3d.res.ContentManager;
import com.g3d.util.BufferUtils;
import de.jarnbjo.ogg.EndOfOggStreamException;
import de.jarnbjo.ogg.LogicalOggStream;
import de.jarnbjo.ogg.OggFormatException;
import de.jarnbjo.ogg.OggPage;
import de.jarnbjo.ogg.PhysicalOggStream;
import de.jarnbjo.ogg.UncachedUrlStream;
import de.jarnbjo.vorbis.CommentHeader;
import de.jarnbjo.vorbis.IdentificationHeader;
import de.jarnbjo.vorbis.VorbisStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class JOGGLoader implements ContentLoader {

    private static int BLOCK_SIZE = 4096*64;

    private PhysicalOggStream oggStream;
    private LogicalOggStream loStream;
    private VorbisStream vorbisStream;

    private CommentHeader commentHdr;
    private IdentificationHeader streamHdr;

    private ByteBuffer readToBuffer() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[512];
        int read = 0;

        try {
            while ( (read = vorbisStream.readPcm(buf, 0, buf.length)) > 0){
                baos.write(buf, 0, read);
            }
        } catch (EndOfOggStreamException ex){
        }

        byte[] dataBytes = baos.toByteArray();
        swapBytes(dataBytes, 0, dataBytes.length);
        ByteBuffer data = BufferUtils.createByteBuffer(dataBytes.length);
        data.put(dataBytes).flip();

//        vorbisStream.close();
//        loStream.close();
//        oggStream.close();

        return data;
    }

    private final void swapBytes(byte[] b, int off, int len) {
        byte tempByte;
        for (int i = off; i < (off+len); i+=2) {
            tempByte = b[i];
            b[i] = b[i+1];
            b[i+1] = tempByte;
        }
    }

    private InputStream readToStream(){
        return new InputStream(){

            private boolean endOfStream = false;

            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public int read(byte[] buf) throws IOException{
                return read(buf,0,buf.length);
            }

            @Override
            public int read(byte[] buf, int offset, int length) throws IOException{
                if (endOfStream)
                    return -1;

                int bytesRead = 0, cnt = 0;

                while (bytesRead < buf.length) {
                    if ((cnt = vorbisStream.readPcm(buf, offset + bytesRead, buf.length - bytesRead)) <= 0) {
                        endOfStream = true;
                        break;
                    }
                    bytesRead += cnt;
                }

                swapBytes(buf, offset, bytesRead);
                return bytesRead;

            }

            @Override
            public void close() throws IOException{
                vorbisStream.close();
            }

        };
    }

    public Object load(ContentManager owner, InputStream stream, String extension, ContentKey key) throws IOException {
        oggStream = new UncachedOggStream(stream);

        Collection<LogicalOggStream> streams = oggStream.getLogicalStreams();
        loStream = streams.iterator().next();

//        if (loStream == null){
//            throw new IOException("OGG File does not contain vorbis audio stream");
//        }

        vorbisStream = new VorbisStream(loStream);

        streamHdr = vorbisStream.getIdentificationHeader();
        commentHdr = vorbisStream.getCommentHeader();

        System.out.println(commentHdr.getTitle());

        String streamProp = owner.getProperty("AudioStreaming");
        boolean readStream = streamProp != null && streamProp.equals("true");
        
        if (!readStream){
            AudioBuffer audioBuffer = new AudioBuffer();
            audioBuffer.setupFormat(streamHdr.getChannels(), 16, streamHdr.getSampleRate());
            audioBuffer.updateData(readToBuffer());
            return audioBuffer;
        }else{
            AudioStream audioStream = new AudioStream();
            audioStream.setupFormat(streamHdr.getChannels(), 16, streamHdr.getSampleRate());
            audioStream.updateData(readToStream());
            return audioStream;
        }
    }

}
