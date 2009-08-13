package com.g3d.res.pack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;

public class HashAlgo {

    public static void main(String[] args) throws IOException{
        GeneralHashFunctionLibrary h = new GeneralHashFunctionLibrary();

        int collisions = 0;
        int wordNum = 0;
        Set<Long> usedHashes = new HashSet<Long>(60000);
        Set<String> usedWords = new HashSet<String>(60000);

        List<String> words = new ArrayList<String>(60000);
        BufferedReader r = new BufferedReader(new InputStreamReader(HashAlgo.class.getResourceAsStream("/words2.txt")));

        long t = 0;
        long total = 0;

        while (r.ready()){
            String ln = r.readLine();
            if (ln == null || ln.equals(""))
                break;

            words.add(ln);
            wordNum ++;
        }
        
            for (String word : words){
                long hash = h.FNVHash(word);
                if (usedHashes.contains(hash)){
                    collisions ++;
                }else{
                    usedHashes.add(hash);
                }
            }
    
        System.out.println("Time: "+total);
        System.out.println("Found "+collisions+" hash collisions in "+wordNum+" words");
    }

}
