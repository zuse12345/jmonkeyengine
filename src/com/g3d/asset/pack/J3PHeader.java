package com.g3d.asset.pack;

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
