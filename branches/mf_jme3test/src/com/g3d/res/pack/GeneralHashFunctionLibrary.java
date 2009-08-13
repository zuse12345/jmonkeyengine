/*
 **************************************************************************
 *                                                                        *
 *          General Purpose Hash Function Algorithms Library              *
 *                                                                        *
 * Author: Arash Partow - 2002                                            *
 * URL: http://www.partow.net                                             *
 * URL: http://www.partow.net/programming/hashfunctions/index.html        *
 *                                                                        *
 * Copyright notice:                                                      *
 * Free use of the General Purpose Hash Function Algorithms Library is    *
 * permitted under the guidelines and in accordance with the most current *
 * version of the Common Public License.                                  *
 * http://www.opensource.org/licenses/cpl.php                             *
 *                                                                        *
 **************************************************************************
*/
package com.g3d.res.pack;

public class GeneralHashFunctionLibrary {

    public long RSHash(String str) {
        int b = 378551;
        int a = 63689;
        long hash = 0;

        for (int i = 0; i < str.length(); i++){
            hash = hash * a + str.charAt(i);
            a = a * b;
        }

        return hash;
    }
    /* End Of RS Hash Function */

    /**
     * Not so good..
     * @param str
     * @return
     */
    public long JSHash(String str) {
        long hash = 1315423911;

        for (int i = 0; i < str.length(); i++){
            hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
        }

        return hash;
    }
    /* End Of JS Hash Function */

    /* End Of  P. J. Weinberger Hash Function */

    /* End Of ELF Hash Function */

    public long BKDRHash(String str) {
        long seed = 131; // 31 131 1313 13131 131313 etc..
        long hash = 0;

        for (int i = 0; i < str.length(); i++){
            hash = (hash * seed) + str.charAt(i);
        }

        return hash;
    }
    /* End Of BKDR Hash Function */

    public long SDBMHash(String str) {
        long hash = 0;

        for (int i = 0; i < str.length(); i++){
            hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
        }

        return hash;
    }
    /* End Of SDBM Hash Function */

    public long DJBHash(String str) {
        long hash = 5381;

        for (int i = 0; i < str.length(); i++){
            hash = ((hash << 5) + hash) + str.charAt(i);
        }

        return hash;
    }
    /* End Of DJB Hash Function */

    /**
     * Not so good..
     * @param str
     * @return
     */
    public long DEKHash(String str) {
        long hash = str.length();

        for (int i = 0; i < str.length(); i++){
            hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
        }

        return hash;
    }
    /* End Of DEK Hash Function */

    /* End Of BP Hash Function */

    public long FNVHash(String str) {
        long fnv_prime = 0x811C9DC5;
        long hash = 0;

        for (int i = 0; i < str.length(); i++){
            hash *= fnv_prime;
            hash ^= str.charAt(i);
        }

        return hash;
    }
    /* End Of FNV Hash Function */

    public long APHash(String str) {
        long hash = 0xAAAAAAAA;

        for (int i = 0; i < str.length(); i++){
            if ((i & 1) == 0){
                hash ^= ((hash << 7) ^ str.charAt(i) * (hash >> 3));
            }else{
                hash ^= (~((hash << 11) + str.charAt(i) ^ (hash >> 5)));
            }
        }

        return hash;
    }
    /* End Of AP Hash Function */
}
