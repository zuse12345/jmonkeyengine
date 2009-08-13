package com.g3d.res.pack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipToJ3p {

    public static final int VERSION = 1;
    public static final int MAX_NAME_LENGTH = Short.MAX_VALUE;

    private HashSet<Integer> usedHashes = new HashSet(100);
    private J3PEntry[] entries;
    private J3PHeader header;

    private Compressor compressor = new Compressor();

    /**
     * Size of the entry table, including the # of entries integer.
     */
    private int entryTableSize = 4;

    private J3PEntry createEntry(ZipEntry entry){
        if (entry == null){
            System.err.println("ZipEntry is null! Ignored.");
            return null;
        }
        if (entry.isDirectory()){
            System.err.println(entry.getName()+" is a directory! Ignored.");
            return null;
        }
        if (entry.getSize() > Integer.MAX_VALUE){
            System.err.println(entry.getName()+" is larger than 2 GB! Ignored.");
            return null;
        }
        if (entry.getName().length() > MAX_NAME_LENGTH){
            System.err.println(entry.getName()+" name is too large! Ignored.");
            return null;
        }

        J3PEntry out = new J3PEntry(entry.getName());
        out.length = (int) entry.getSize();
        entryTableSize += 4 + 1 + 4 + 4;
        if (usedHashes.contains(out.hash)){
            out.flags |= J3PEntry.KEY_INCLUDED;
            entryTableSize += 2 + out.name.length();
        }else{
            usedHashes.add(out.hash);
        }

        return out;
    }

    private void writeEntry(J3PEntry entry, ByteBuffer store){
        if (entry == null)
            return;
        
        store.putInt(entry.hash);
        store.put((byte)entry.flags);
        if ((entry.flags & J3PEntry.KEY_INCLUDED) != 0){
            byte[] utf8 = null;
            try{
                utf8 = entry.name.getBytes("UTF-8");
            }catch (UnsupportedEncodingException ex){
                ex.printStackTrace();
            }
            store.putShort((short) entry.name.length());
//            for (int i = 0; i < entry.name.length(); i++){
//                store.put((byte) entry.name.charAt(i));
//            }
            store.put(utf8);
        }
        store.putInt((int) entry.offset);
        store.putInt(entry.length);
    }

    

    private long writeEntryData(J3PEntry entry, InputStream in, FileChannel out) throws IOException{
        if (entry == null)
            return 0;

        long t = System.nanoTime();
        long initialPos = out.position();
        long read = entry.length;

        compressor.compress(in, out, entry);

        long finalPos = out.position();
        long length = finalPos - initialPos;

        System.out.println("------------");

        System.out.println("Entry "+entry.name);

        double seconds = ((double) (System.nanoTime() - t)) / 1000000000.0;
        double mbs = (length / 1024.0) / 1024.0;
        System.out.format("MB/s = %9.3f\n", mbs / seconds);
        System.out.println("Read: "+read+", Written: "+length);
        System.out.format("Compression ratio: %1.0f%%\n", (double)length / read * 100.0);
        
        entry.offset = initialPos - header.dataOffset;
        entry.length = (int) length;
        System.out.println(entry.name + "[offset="+entry.offset+", length="+entry.length+"]");
        System.out.println("------------");

        return length;
    }

    private ByteBuffer createTable(){
        ByteBuffer tableBuf = ByteBuffer.allocate(entryTableSize);
        tableBuf.order(ByteOrder.LITTLE_ENDIAN);
        tableBuf.putInt(entries.length);
        for (J3PEntry entry : entries){
            writeEntry(entry, tableBuf);
        }
        tableBuf.flip();

        return tableBuf;
    }

    private void writeHeader(ByteBuffer store){
        store.put(header.signature);
        store.putInt(header.version);
        store.putInt(header.flags);
        store.putInt(header.dataOffset);
    }

    public void create(File inFile, File outFile) throws IOException {
        System.out.println("Zip To J3P Version "+VERSION);

        header = new J3PHeader();
        header.version = VERSION;

        // Open Zip file
        ZipFile zf = new ZipFile(inFile, ZipFile.OPEN_READ);

        // Create entries
        entries = new J3PEntry[zf.size()];
        int idx = 0;
        Enumeration<ZipEntry> zipEntries = (Enumeration<ZipEntry>) zf.entries();
        while (zipEntries.hasMoreElements()){
            ZipEntry zipEntry = zipEntries.nextElement();
            entries[idx++] = createEntry(zipEntry);
        }

        // sig + version + flags + data offset
        int headerSize = 4 + 4 + 4 + 4;

        // header size + table size
        header.dataOffset = headerSize + entryTableSize;

        // WRITE ALL DATA
        FileChannel chan = new RandomAccessFile(outFile, "rw").getChannel();
        chan.truncate(header.dataOffset);

        // Go to data offset
        chan.position(header.dataOffset);

        for (int i = 0; i < entries.length; i++){
            if (entries[i] == null)
                continue;

            J3PEntry entry = entries[i];
            InputStream in = zf.getInputStream(zf.getEntry(entry.name));
            writeEntryData(entry, in, chan);
            in.close();
        }
        zf.close();

        long fileSize = chan.position();

        // Generate header
        ByteBuffer headerBuf = ByteBuffer.allocate(headerSize);
        headerBuf.order(ByteOrder.LITTLE_ENDIAN);
        writeHeader(headerBuf);
        headerBuf.flip();

        // Generate entry table
        ByteBuffer tableBuf = createTable();

        chan.position(0);
        chan.write(new ByteBuffer[]{ headerBuf, tableBuf });

        System.out.println(fileSize+", "+chan.size());

        assert chan.size() == fileSize;
        chan.close();
    }

    public static void main(String[] args) throws IOException{
        if (args.length == 0){
            System.out.println("Usage: java ZipToJ3p <input zip>");
            System.exit(1);
        }

        ZipToJ3p ztj = new ZipToJ3p();
//        while (true)
            ztj.create(new File(args[0]), new File(args[0]+".j3p"));
    }

}
