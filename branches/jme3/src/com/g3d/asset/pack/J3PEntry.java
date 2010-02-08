package com.g3d.asset.pack;

/**
 * Represents a single file entry in a J3P (jME3 pack) file.
 */
public class J3PEntry extends NamedEntry {

    /**
     * When compressing, indicates that the 'name' parameter
     * should be stored. When decompressing, indicates that a
     * 'name' parameter should be read and used for comparison
     * since the entry had a hash collision with another entry.
     */
    static final int KEY_INCLUDED = 0x1;

    /**
     * Indicates the entry data is DEFLATE compressed.
     * 'length' contains the compressed data size.
     */
    static final int DEFLATE_COMPRESSED = 0x8;

    static final int GZIP_COMPRESSED = 0x10;

    static final int PACK200_DEFLATE_COMPRESSED = 0x20;

    static final int LZMA_COMPRESSED = 0x40;

    static final int PACK200_LZMA_COMPRESSED = 0x80;

    /**
     * Flags. See int constants in this class for more info.
     */
    int flags;

    /**
     * Offset of this file entry's data, from the beginning of the data chunk.
     */
    long offset;

    /**
     * Length, in bytes, of the stored file.
     */
    int length;

    J3PEntry(String name){
        super(name);
    }

    J3PEntry(int hash){
        super(hash);
    }

}
