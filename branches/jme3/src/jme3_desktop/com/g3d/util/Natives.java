package com.g3d.util;

import com.g3d.system.AppSettings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Helper class for extracting the natives (dll, so) from the jars.
 * This class should only be used internally.
 */
public class Natives {

    private static File workingDir;
    private static final Logger logger = Logger.getLogger(Natives.class.getName());

    static {
        workingDir = new File(System.getProperty("user.dir"));
        if (!workingDir.exists())
            throw new RuntimeException("Working directory "+workingDir+" does not exist!");
    }

    public static void extractNativeLib(String sysName, String name) throws IOException{
        String fullname = System.mapLibraryName(name);
        File targetFile = new File(workingDir, fullname);
        if (targetFile.exists())
            return;

        InputStream in = Natives.class.getResourceAsStream("/native/"+sysName+"/" + fullname);
        if (in == null) {
            logger.warning("Cannot locate native library " + name);
            return;
        }
        
        OutputStream out = new FileOutputStream(targetFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        logger.fine("Copied "+fullname+" to "+targetFile);
    }

    public static void extractNativeLibs(String sysName, AppSettings settings) throws IOException{
        // TODO: it seems JOGL has a different method of loading
        // natives that is incompatible with LWJGLs..
        // In order to properly support JOGL, the architecture (32 or 64 bit)
        // must be determined and then the proper natives can be extracted. 
//        extractNativeLib(sysName, "jogl");
//        extractNativeLib(sysName, "jogl64");
//        extractNativeLib(sysName, "jogl_awt");
        if (settings.useJoysticks()){
            if (sysName.equals("windows")){
                extractNativeLib(sysName, "jinput-dx8");
                extractNativeLib(sysName, "jinput-dx8_64");
                extractNativeLib(sysName, "jinput-raw");
                extractNativeLib(sysName, "jinput-raw_64");
            }else if (sysName.equals("linux")){
                extractNativeLib(sysName, "jinput-linux");
                extractNativeLib(sysName, "jinput-linux64");
            }else{
                extractNativeLib(sysName, "jinput");
                extractNativeLib(sysName, "jinput64");
            }
        }

        String renderer = settings.getRenderer();
        String audioRenderer = settings.getAudioRenderer();
        boolean needLWJGL = false;
        if (renderer != null){
            if (renderer.startsWith("LWJGL")){
                needLWJGL = true;
            }
        }
        if (audioRenderer != null){
            if (audioRenderer.equals("LWJGL")){
                needLWJGL = true;
            }
        }
        if (needLWJGL){
            extractNativeLib(sysName, "lwjgl");
            extractNativeLib(sysName, "lwjgl64");
            if (audioRenderer != null){
                extractNativeLib(sysName, "OpenAL32");
                extractNativeLib(sysName, "OpenAL64");
            }
            System.setProperty("org.lwjgl.librarypath", workingDir.toString());
        }else{
            // user must set property java.library.path  ..
            // System.setProperty("java.library.path", workingDir.toString());
        }
    }

}
