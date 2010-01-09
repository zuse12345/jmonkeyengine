package com.g3d.asset.pack;

import com.g3d.util.LittleEndien;
import com.lzma.LzmaInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class J3PHttpFile {

    private static final int VERSION = 1;
    private static final Logger logger = Logger.getLogger(J3PHttpFile.class.getName());

    private HashMap<NamedEntry, J3PEntry> entryMap;
    private URL url;
    private StringBuilder sb = new StringBuilder();
    private final byte[] buffer = new byte[4096];

    public static enum Access {
        Copy,
        Parse,
        Stream
    }

    public J3PHttpFile(){
    }

    private InputStream requestRange(long offset, long length){
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);

            sb.setLength(0);
            sb.append("bytes=");
            sb.append(offset);
            sb.append('-');
            sb.append(offset+length-1);
            
            conn.setRequestProperty("Range", sb.toString());
            conn.connect();

            if (conn.getResponseCode() == 416)
                throw new RuntimeException("Formatting error has occured.. " +
                                           "Unexpected!");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL)
                throw new RuntimeException("Your server does not support the" +
                                           " 'Range' http parameter! Contact " +
                                           "your webserver administrator for " +
                                           "more details");

            int len = conn.getContentLength();
            if (len != length)
                throw new RuntimeException("Expected length: "+length+", got "+len);


            InputStream in = conn.getInputStream();
            return in;
        }catch (IOException ex){
            logger.log(Level.SEVERE, "Failed to open URL: "+url, ex);
        }

        return null;
    }

    private void readHeader(J3PHeader header) throws IOException{
        LittleEndien le = new LittleEndien(requestRange(0, 16));

        // signature reversed here for little endien
        if (le.readInt() != 0x3050334a){
            throw new IOException("File is not a J3P file");
        }

        header.version = le.readInt();
        if (header.version != VERSION){
            logger.warning("Possible version incompatability in J3P file");
        }
        
        header.flags = le.readInt();
        header.dataOffset = le.readInt();
    }

    private J3PEntry readEntry(LittleEndien le) throws IOException {
        int hash = le.readInt();
        int flags = le.readByte();

        J3PEntry entry;
        if ((flags & J3PEntry.KEY_INCLUDED) != 0){
            int nameLength = le.readShort();
            byte[] nameBytes = new byte[nameLength];
            le.readFully(nameBytes);
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
        entry.offset = le.readInt();
        entry.length = le.readInt();

        return entry;
    }

    private void readTable(J3PTable table, int dataOffset, int tableLength) throws IOException {
        LittleEndien le = new LittleEndien(requestRange(16, 16 + tableLength - 1));

        int entryCount = le.readInt();
        table.init(entryCount);

        for (int i = 0; i < entryCount; i++){
            J3PEntry entry = readEntry(le);
            entry.offset += dataOffset; // so that it doesn't have to be added later
            table.setEntry(i, entry);
        }
    }

    public static void main(String[] args){
        J3PHttpFile http = new J3PHttpFile();
        try{
            http.open(new URL("http://mfkarpg.110mb.com/Q3.bin"));
            InputStream in = http.openStream(http.findEntry("main.meshxml"), "main.meshxml", Access.Parse);
            OutputStream out = new FileOutputStream("C:\\main.meshxml");
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }catch (Throwable ex){
            Logger.getLogger(J3PHttpFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void open(URL url) throws IOException{
        this.url = url;

        J3PHeader header = new J3PHeader();
        readHeader(header);

        J3PTable table = new J3PTable();
        readTable(table, header.dataOffset, header.dataOffset - 16);

        entryMap = new HashMap<NamedEntry, J3PEntry>(table);
    }

    private J3PEntry findEntry(int hash){
        if (url == null)
            throw new IllegalStateException("The J3PFile is not open");

        NamedEntry namedEntry = new NamedEntry(hash);
        return entryMap.get(namedEntry);
    }

    private J3PEntry findEntry(String name){
        if (url == null)
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

    private InputStream openStream(J3PEntry entry, String name, Access access){
        try{
            InputStream entryChan = requestRange(entry.offset, entry.length);
            if ((entry.flags & J3PEntry.LZMA_COMPRESSED) != 0){
                // wrap with LZMA decomressor
                return new LzmaInputStream(entryChan);
            }else{
                return entryChan;
            }
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Failed to open stream: " + name, ex);
        }

        return null;
    }

    private ReadableByteChannel openChannel(J3PEntry entry, String name, Access access){
        if (access == Access.Copy)
            throw new IllegalArgumentException("Illegal access mode for channel: Copy");
        
        return Channels.newChannel(openStream(entry, name, access));
    }

    private ByteBuffer openBuffer(J3PEntry entry, String name, Access access){
        if (access == Access.Stream)
            throw new IllegalArgumentException("Illegal access mode for buffer: Stream");

        try {
            ByteBuffer bb = ByteBuffer.allocateDirect(entry.length);

            InputStream in = openStream(entry, name, access);
            int len;
            while ((len = in.read(buffer)) > 0) {
                bb.put(buffer, 0, len);
            }
            in.close();

            bb.clear();
            return bb;
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to open buffer: "+name, ex);
        }
        return null;
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
        url = null;
        entryMap = null;
    }

}
