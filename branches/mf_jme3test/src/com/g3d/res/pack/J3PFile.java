package com.g3d.res.pack;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class J3PFile {

    private static final int VERSION = 1;
    private static final boolean USE_FILE_MAPPING = true;
    private static final Logger logger = Logger.getLogger(J3PFile.class.getName());

    private HashMap<NamedEntry, J3PEntry> entryMap;
    private FileChannel channel;

    public static enum Access {
        Copy,
        Parse,
        Stream
    }

    public J3PFile(){
    }

    private void readHeader(J3PHeader header, FileChannel chan) throws IOException{
        ByteBuffer headerBuf = ByteBuffer.allocate(16);
        chan.read(headerBuf);
        headerBuf.flip();
        
        if (headerBuf.getInt() != header.signatureInt){
            throw new IOException("File is not a J3P file");
        }

        header.version = headerBuf.getInt();
        if (header.version != VERSION){
            logger.warning("Possible version incompatability in J3P file");
        }
        
        header.flags = headerBuf.getInt();
        header.dataOffset = headerBuf.getInt();
    }

    private J3PEntry readEntry(ByteBuffer tableBuf) {
        int hash = tableBuf.getInt();
        int flags = tableBuf.get();

        J3PEntry entry;
        if ((flags & J3PEntry.KEY_INCLUDED) != 0){
            int nameLength = tableBuf.getShort();
            byte[] nameBytes = new byte[nameLength];
            tableBuf.get(nameBytes);
            try{
                String name = new String(nameBytes, "UTF-8");
                entry = new J3PEntry(name);
            }catch (UnsupportedEncodingException ex){
                logger.log(Level.SEVERE, "Failed to load encoding UTF-8", ex);
                entry = null;
            }
        }else{
            entry = new J3PEntry(hash);
        }

        entry.offset = tableBuf.getInt();
        entry.length = tableBuf.getInt();

        return entry;
    }

    private void readTable(J3PTable table, int tableLength, FileChannel chan) throws IOException {
        ByteBuffer tableBuf = ByteBuffer.allocate(tableLength);
        chan.read(tableBuf);
        tableBuf.flip();

        int entryCount = tableBuf.getInt();
        table.init(entryCount);

        for (int i = 0; i < entryCount; i++){
            J3PEntry entry = readEntry(tableBuf);
            table.setEntry(i, entry);
        }
    }

    public void open(FileChannel chan) throws IOException{
        J3PHeader header = new J3PHeader();
        readHeader(header, chan);

        J3PTable table = new J3PTable();
        readTable(table, header.dataOffset - 16, chan);

        entryMap = new HashMap<NamedEntry, J3PEntry>(table);
        channel = chan;
    }

    private J3PEntry findEntry(String name){
        if (channel == null)
            throw new IllegalStateException("The J3PFile is not open");

        if (name == null)
            throw new NullPointerException();

        if (name.equals(""))
            throw new IllegalArgumentException("Name cannot be empty string");

        NamedEntry namedEntry = new NamedEntry(name);
        return entryMap.get(namedEntry);
    }

    public ByteBuffer openBuffer(String name, Access access){
        J3PEntry entry = findEntry(name);
        if (entry == null)
            return null;

        if (access == Access.Stream)
            throw new IllegalArgumentException("Illegal access mode for buffer: Stream");

        try {
            if (USE_FILE_MAPPING){
                MappedByteBuffer mbb = channel.map(MapMode.READ_ONLY, entry.offset, entry.length);
                if (access == Access.Copy)
                    mbb.load();

                mbb.clear();
                return mbb;
            }else{
                ByteBuffer bb = ByteBuffer.allocateDirect(entry.length);
                channel.read(bb, entry.offset);
                bb.clear();
                return bb;
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to open buffer: "+name, ex);
        }
        return null;
    }

    public ReadableByteChannel openChannel(String name, Access access){
        J3PEntry entry = findEntry(name);
        if (entry == null)
            return null;

        if (access == Access.Copy)
            throw new IllegalArgumentException("Illegal access mode for channel: Copy");

       try{
            if (USE_FILE_MAPPING){
                if (access == Access.Parse){
                    MappedByteBuffer mbb = channel.map(MapMode.READ_ONLY, entry.offset, entry.length);
                    mbb.clear();
                    return new ReadableBufferChannel(mbb);
                }else{
                    return new FileRangeChannel(channel, entry.offset, entry.length);
                }
            } else {
                if (access == Access.Parse){
                    ByteBuffer bb = ByteBuffer.allocateDirect(entry.length);
                    channel.read(bb, entry.offset);
                    bb.clear();
                    return new ReadableBufferChannel(bb);
                }else{
                    return new FileRangeChannel(channel, entry.offset, entry.length);
                }
            }
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Failed to open buffer: " + name, ex);
        }
        return null;
    }

    public void close() throws IOException{
        channel.close();
        entryMap = null;
        channel = null;
    }

}
