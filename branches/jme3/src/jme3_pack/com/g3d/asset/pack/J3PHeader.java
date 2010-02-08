package com.g3d.asset.pack;

/**
 * The header in a J3P file. Contains a FourCC signature, version integer
 * flags, and the data chunk byte offset.
 * @author Kirill
 */
class J3PHeader {

    final byte[] signature = new byte[]{ (byte)'J',
                                   (byte)'3',
                                   (byte)'P', 
                                   (byte)'0' };

    final int signatureInt = 0x4A335030;
    
    int version;
    int flags;
    int dataOffset;
    
}
