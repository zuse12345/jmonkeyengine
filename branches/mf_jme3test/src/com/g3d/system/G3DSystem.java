package com.g3d.system;

import com.g3d.util.G3DFormatter;
import com.g3d.*;
import com.g3d.audio.AudioRenderer;
import com.g3d.audio.joal.JoalAudioRenderer;
import com.g3d.audio.lwjgl.LwjglAudioRenderer;
import com.g3d.system.jogl.JoglDisplay;
import com.g3d.system.lwjgl.LwjglDisplay;
import com.g3d.system.lwjgl.LwjglOffscreenBuffer;
import com.g3d.util.Natives;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class G3DSystem {

    private static final Logger logger = Logger.getLogger(G3DSystem.class.getName());

    private static boolean initialized = false;
    
    public static boolean trackDirectMemory(){
        return false;
    }

    public static String getPlatformID(){
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")){
            return "windows";
        }else if (os.contains("linux") || os.contains("freebsd") || os.contains("sunos")){
            return "linux";
        }else if (os.contains("mac os x")){
            return "macosx";
        }else{
            throw new UnsupportedOperationException("The specified platform: "+os+" is not supported.");
        }
    }

    public static G3DContext newContext(AppSettings settings, G3DContext.Type contextType) {
        initialize(settings);
        G3DContext ctx;
        if (settings.getRenderer().startsWith("LWJGL")){
            if (contextType == G3DContext.Type.OffscreenSurface){
                ctx = new LwjglOffscreenBuffer();
            }else{
                ctx = new LwjglDisplay();
            }
            ctx.setSettings(settings);
        }else if (settings.getRenderer().startsWith("JOGL")){
            ctx = new JoglDisplay();
            ctx.setSettings(settings);
        }else{
            throw new UnsupportedOperationException(
                            "Unrecognizable renderer specified: "+
                            settings.getRenderer());
        }
        return ctx;
    }

    public static AudioRenderer newAudioRenderer(AppSettings settings){
        initialize(settings);
        AudioRenderer ar;
        if (settings.getAudioRenderer().startsWith("LWJGL")){
            ar = new LwjglAudioRenderer();
        }else if (settings.getAudioRenderer().startsWith("JOAL")){
            ar = new JoalAudioRenderer();
        }else{
            throw new UnsupportedOperationException(
                            "Unrecognizable audio renderer specified: "+
                            settings.getAudioRenderer());
        }
        return ar;
    }

    public static void initialize(AppSettings settings){
        if (initialized)
            return;
        
        initialized = true;
        try {
            Handler fileHandler = new FileHandler("jme.log");
            Handler consoleHandler = new ConsoleHandler();
            G3DFormatter formatter = new G3DFormatter();
            fileHandler.setFormatter(formatter);
            consoleHandler.setFormatter(formatter);
            Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
            Logger.getLogger("").addHandler(fileHandler);
            Logger.getLogger("").addHandler(consoleHandler);
            Logger.getLogger("com.g3d").setLevel(Level.FINEST);
        } catch (IOException ex){
            logger.log(Level.SEVERE, "I/O Error while creating log file", ex);
        } catch (SecurityException ex){
            logger.log(Level.SEVERE, "Security error in creating log file", ex);
        }
        logger.info("Running on "+getFullName());

        String val = System.getProperty("jnlp.g3d.nonativecopy");
        if (val == null || !val.equals("true")){
            try {
                Natives.extractNativeLibs(getPlatformID(), settings);
            } catch (IOException ex) {
                ex.printStackTrace();
                //reportError("Error while copying native libraries", ex);
            }
        }
    }

//    public static void destroy() {
//        if (!initialized)
//            return;
//
//        initialized = false;
//        logger.finer(getFullName() + " closing.");
//    }

    public static String getFullName(){
        return "jMonkey Engine 3 ALPHA 0.25";
    }

   

}
