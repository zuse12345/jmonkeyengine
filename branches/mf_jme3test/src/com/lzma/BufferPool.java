package com.lzma;

import java.util.ArrayList;

public class BufferPool {

    private static final ArrayList<byte[]> bufferQueue = new ArrayList<byte[]>();
    private static final ArrayList[] dictQueues = new ArrayList[30];

    public static byte[] aquireBuffer(){
        synchronized (bufferQueue){
            if (bufferQueue.size() == 0){
                return new byte[LzmaReadableChannel.kBlockSize];
            }else{
                return bufferQueue.remove(bufferQueue.size()-1);
            }
        }
    }

    public static void returnBuffer(byte[] buf){
        synchronized (bufferQueue){
            bufferQueue.add(buf);
        }
    }

    private static int ilog2(int x) {
        int l=0;
        if(x >= 1<<16) { x>>=16; l|=16; }
        if(x >= 1<<8) { x>>=8; l|=8; }
        if(x >= 1<<4) { x>>=4; l|=4; }
        if(x >= 1<<2) { x>>=2; l|=2; }
        if(x >= 1<<1) l|=1;
        return l;
    }

    public static void returnDict(byte[] dict){
        int lg = ilog2(dict.length);
        ArrayList lst = dictQueues[lg];
        if (lst == null){
            lst = new ArrayList();
            dictQueues[lg] = lst;
        }
        lst.add(dict);

    }

    public static byte[] aquireDict(int sz){
        ArrayList lst = dictQueues[ilog2(sz)];
        if (lst == null || lst.size() == 0){
           return new byte[sz];
        }else{
            return (byte[]) lst.remove(lst.size()-1);
        }
    }

}
