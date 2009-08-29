package com.g3d.asset.pack;

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


    int flags;
    long offset;
    int length;

    J3PEntry(String name){
        super(name);
    }

    J3PEntry(int hash){
        super(hash);
    }

}
