package jme3tools.deploy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;

public class JarCompressor {

    private static final Packer pack = Pack200.newPacker();

    static {
        Map<String, String> p = pack.properties();
        p.put(Packer.EFFORT, "9");
        p.put(Packer.KEEP_FILE_ORDER, Packer.FALSE);
        p.put(Packer.MODIFICATION_TIME, Packer.LATEST);
        p.put(Packer.DEFLATE_HINT, Packer.FALSE);
        p.put(Packer.CODE_ATTRIBUTE_PFX+"LineNumberTable", Packer.STRIP);
        p.put(Packer.UNKNOWN_ATTRIBUTE, Packer.ERROR);
    }

    public static final void pklz(JarInputStream jarData, OutputStream pklzData) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pack.pack(jarData, baos);
        baos.close();

        byte[] packData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(packData);
    }
}
