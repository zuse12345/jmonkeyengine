package com.g3d.asset.pack;

import com.lzma.LzmaReadableChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class J3PFile extends J3P {

    private static final int VERSION = 1;
    private static final boolean USE_FILE_MAPPING = false;
    private static final Logger logger = Logger.getLogger(J3PFile.class.getName());

    private HashMap<NamedEntry, J3PEntry> entryMap;
    private FileChannel channel;

    /**
     * Test method. Extract all files in the J3P file given in the args 
     * into the current directory.
     */
    public static void main(String[] args) throws IOException{
        File f = new File(args[0]);
        FileInputStream fis = new FileInputStream(f);

        J3PFile jf = new J3PFile();
        jf.open(fis.getChannel());

        for (int hash : jf.getEntryHashes()){
            File outFile = new File(f.getParentFile(), Integer.toHexString(hash).toUpperCase()+".bin");
            outFile.getParentFile().mkdirs();

            ReadableByteChannel inChan = jf.openChannel(hash, Access.Parse);
            FileChannel outChan = new FileOutputStream(outFile).getChannel();
            long read;
            long len = Long.MAX_VALUE;
            while (true){
                read = outChan.transferFrom(inChan, 0, len);
                if (read <= 0)
                    break;

                len -= read;
            }

            outChan.close();
        }
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

        headerBuf.order(ByteOrder.LITTLE_ENDIAN);
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

        entry.flags = flags;
        entry.offset = tableBuf.getInt();
        entry.length = tableBuf.getInt();

        return entry;
    }

    private void readTable(J3PTable table, int dataOffset, int tableLength, FileChannel chan) throws IOException {
        ByteBuffer tableBuf = ByteBuffer.allocate(tableLength);
        chan.read(tableBuf);
        tableBuf.flip();

        tableBuf.order(ByteOrder.LITTLE_ENDIAN);
        int entryCount = tableBuf.getInt();
        table.init(entryCount);

        for (int i = 0; i < entryCount; i++){
            J3PEntry entry = readEntry(tableBuf);
            entry.offset += dataOffset; // so that it doesn't have to be added later
            table.setEntry(i, entry);
        }
    }

    public void open(FileChannel chan) throws IOException{
        J3PHeader header = new J3PHeader();
        readHeader(header, chan);

        J3PTable table = new J3PTable();
        readTable(table, header.dataOffset, header.dataOffset - 16, chan);

        entryMap = new HashMap<NamedEntry, J3PEntry>(table);
        channel = chan;
    }

    public void open(File file) throws IOException {
        File fileExt = file.getAbsoluteFile();
        open(new FileInputStream(fileExt).getChannel());
    }

    private J3PEntry findEntry(int hash){
        if (channel == null)
            throw new IllegalStateException("The J3PFile is not open");

        NamedEntry namedEntry = new NamedEntry(hash);
        return entryMap.get(namedEntry);
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

    public Iterable<Integer> getEntryHashes(){
        final Iterator<NamedEntry> entryIter = entryMap.keySet().iterator();
        final Iterator<Integer> nameIter = new Iterator<Integer>(){
            public boolean hasNext() {
                return entryIter.hasNext();
            }
            public Integer next() {
                return entryIter.next().hash;
            }
            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
        return new Iterable<Integer>(){
            public Iterator<Integer> iterator() {
                return nameIter;
            }
        };
    }

    public int getEntrySize(String name){
        J3PEntry entry = findEntry(name);
        if (entry == null)
            return -1;
        
        return entry.length;
    }

    public int getEntrySize(int hash){
        J3PEntry entry = findEntry(hash);
        if (entry == null)
            return -1;

        return entry.length;
    }

    private ByteBuffer openBuffer(J3PEntry entry, String name, Access access){
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

    private InputStream openStream(J3PEntry entry, String name, Access access){
        if (access == Access.Copy)
            throw new IllegalArgumentException("Illegal access mode for stream: Copy");

        InputStream in;
//        try{
//            if (USE_FILE_MAPPING){
//                if (access == Access.Parse){
//                    MappedByteBuffer mbb = channel.map(MapMode.READ_ONLY, entry.offset, entry.length);
//                    mbb.clear();
//                    if (mbb.capacity() != entry.length){
//                        logger.severe("File mapping failed: "+mbb.capacity()+" != "+entry.length);
//                        return null;
//                    }
//                    entryChan = new ReadableBufferChannel(mbb);
//                }else{
//                    entryChan = new FileRangeChannel(channel, entry.offset, entry.length);
//                }
//            } else {
//                if (access == Access.Parse){
//                    ByteBuffer bb = ByteBuffer.allocateDirect(entry.length);
//                    channel.read(bb, entry.offset);
//                    bb.clear();
//                    entryChan = new ReadableBufferChannel(bb);
//                }else{
//                    entryChan = new FileRangeChannel(channel, entry.offset, entry.length);
//                }
//            }
//            if ((entry.flags & J3PEntry.LZMA_COMPRESSED) != 0){
//                // wrap with LZMA decomressor
//                return new LzmaReadableChannel(entryChan);
//            }else{
//                return entryChan;
//            }
//        } catch (IOException ex){
//            logger.log(Level.SEVERE, "Failed to open buffer: " + name, ex);
//        }
        return null;
    }

    private ReadableByteChannel openChannel(J3PEntry entry, String name, Access access){
        if (access == Access.Copy)
            throw new IllegalArgumentException("Illegal access mode for channel: Copy");

        ReadableByteChannel entryChan;
       try{
            if (USE_FILE_MAPPING){
                if (access == Access.Parse){
                    MappedByteBuffer mbb = channel.map(MapMode.READ_ONLY, entry.offset, entry.length);
                    mbb.clear();
                    if (mbb.capacity() != entry.length){
                        logger.severe("File mapping failed: "+mbb.capacity()+" != "+entry.length);
                        return null;
                    }
                    entryChan = new ReadableBufferChannel(mbb);
                }else{
                    entryChan = new FileRangeChannel(channel, entry.offset, entry.length);
                }
            } else {
                if (access == Access.Parse){
                    ByteBuffer bb = ByteBuffer.allocateDirect(entry.length);
                    channel.read(bb, entry.offset);
                    bb.clear();
                    entryChan = new ReadableBufferChannel(bb);
                }else{
                    entryChan = new FileRangeChannel(channel, entry.offset, entry.length);
                }
            }
            if ((entry.flags & J3PEntry.LZMA_COMPRESSED) != 0){
                // wrap with LZMA decomressor
                return new LzmaReadableChannel(entryChan);
            }else{
                return entryChan;
            }
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Failed to open buffer: " + name, ex);
        }
        return null;
    }

    public InputStream openStream(String name, Access access){
        J3PEntry entry = findEntry(name);
        if (entry == null)
            return null;

        return openStream(entry, name, access);
    }

    public InputStream openStream(int hash, Access access){
        J3PEntry entry = findEntry(hash);
        if (entry == null)
            return null;

        return openStream(entry, Integer.toHexString(hash), access);
    }

    public ReadableByteChannel openChannel(String name, Access access){
        J3PEntry entry = findEntry(name);
        if (entry == null)
            return null;

        return openChannel(entry, name, access);
    }

    public ReadableByteChannel openChannel(int hash, Access access){
        J3PEntry entry = findEntry(hash);
        if (entry == null)
            return null;

        return openChannel(entry, Integer.toHexString(hash), access);
    }

    public ByteBuffer openBuffer(String name, Access access){
        J3PEntry entry = findEntry(name);
        if (entry == null)
            return null;

        return openBuffer(entry, name, access);
    }

    public ByteBuffer openBuffer(int hash, Access access){
        J3PEntry entry = findEntry(hash);
        if (entry == null)
            return null;

        return openBuffer(entry, Integer.toHexString(hash), access);
    }

    public void close() throws IOException{
        channel.close();
        entryMap = null;
        channel = null;
    }

}
