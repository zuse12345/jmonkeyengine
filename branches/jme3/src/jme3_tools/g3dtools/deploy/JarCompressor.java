package g3dtools.deploy;

import SevenZip.Compression.LZMA.Encoder;
import SevenZip.LzmaAlone;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;

public class JarCompressor {

    private static final Encoder jarLzma = new Encoder();
    private static final Packer pack = Pack200.newPacker();

    static {
        Map<String, String> p = pack.properties();
        p.put(Packer.EFFORT, "9");
        p.put(Packer.KEEP_FILE_ORDER, Packer.FALSE);
        p.put(Packer.MODIFICATION_TIME, Packer.LATEST);
        p.put(Packer.DEFLATE_HINT, Packer.FALSE);
        p.put(Packer.CODE_ATTRIBUTE_PFX+"LineNumberTable", Packer.STRIP);
        p.put(Packer.UNKNOWN_ATTRIBUTE, Packer.ERROR);

        jarLzma.SetNumFastBytes(128);
        // Fast bytes, aka Word Size [5, 273], default: 128

        jarLzma.SetAlgorithm(2);
        // 1 = normal, 0 = fast

        jarLzma.SetEndMarkerMode(false);
        // Not needed if size is written at the beginning of the file (default)

        jarLzma.SetLcLpPb(3, 0, 2);
        // Literal context bits Lc [0, 8], default: 3 (4 may help for big files)
        // Literal position bits Lp [0, 4], default: 0
        // Position bits Pb [0, 4], default: 2

        jarLzma.SetMatchFinder(1);
        // bt2  -> 0
        // bt4  -> 1
        // bt4b -> 2

        jarLzma.SetDictionarySize(1 << 22); // aka 2^22 bytes = 4 MB
    }

    public static final void pklz(JarInputStream jarData, OutputStream pklzData) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pack.pack(jarData, baos);
        baos.close();

        byte[] packData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(packData);

        jarLzma.WriteCoderProperties(pklzData);
        long size = packData.length;
        for (int i = 0; i < 8; i++)
            pklzData.write((int)(size >>> (8 * i)) & 0xFF);

        jarLzma.Code(bais, pklzData, -1, -1, null);
    }
}
