package com.g3d.system;

import com.g3d.util.G3DFormatter;
import com.g3d.*;
import com.g3d.audio.AudioRenderer;
import com.g3d.audio.joal.JoalAudioRenderer;
import com.g3d.audio.lwjgl.LwjglAudioRenderer;
import com.g3d.system.jogl.JoglDisplay;
import com.g3d.system.lwjgl.LwjglCanvas;
import com.g3d.system.lwjgl.LwjglDisplay;
import com.g3d.system.lwjgl.LwjglOffscreenBuffer;
import com.g3d.util.Natives;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class G3DSystem {

    private static final Logger logger = Logger.getLogger(G3DSystem.class.getName());

    private static boolean initialized = false;
    private static boolean lowPermissions = false;
    
    public static boolean trackDirectMemory(){
        return false;
    }

    public static void setLowPermissions(boolean lowPerm){
        lowPermissions = lowPerm;
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

    private static G3DContext newContextLwjgl(AppSettings settings, G3DContext.Type type){
        switch (type){
            case Canvas:
                return new LwjglCanvas();
            case Display:
                return new LwjglDisplay();
            case OffscreenSurface:
                return new LwjglOffscreenBuffer();
            default:
                throw new IllegalArgumentException("Unsupported context type "+type);
        }
    }

    public static G3DContext newContextJogl(AppSettings settings, G3DContext.Type type){
        switch (type) {
            case Display:
                return new JoglDisplay();
            default:
                throw new IllegalArgumentException("Unsupported context type "+type);
        }
    }

    public static G3DContext newContext(AppSettings settings, G3DContext.Type contextType) {
        initialize(settings);
        G3DContext ctx;
        if (settings.getRenderer().startsWith("LWJGL")){
            ctx = newContextLwjgl(settings, contextType);
            ctx.setSettings(settings);
        }else if (settings.getRenderer().startsWith("JOGL")){
            ctx = newContextJogl(settings, contextType);
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
            G3DFormatter formatter = new G3DFormatter();

            if (!lowPermissions){
                Handler fileHandler = new FileHandler("jme.log");
                fileHandler.setFormatter(formatter);
                Logger.getLogger("").addHandler(fileHandler);
            }
            
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(formatter);
            Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
            Logger.getLogger("").addHandler(consoleHandler);
            Logger.getLogger("com.g3d").setLevel(Level.FINEST);
        } catch (IOException ex){
            logger.log(Level.SEVERE, "I/O Error while creating log file", ex);
        } catch (SecurityException ex){
            logger.log(Level.SEVERE, "Security error in creating log file", ex);
        }
        logger.info("Running on "+getFullName());

        
        if (!lowPermissions){
            try {
                Natives.extractNativeLibs(getPlatformID(), settings);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error while copying native libraries", ex);
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
        return "jMonkey Engine 3 ALPHA 0.30";
    }

   

}
