package com.g3d.system;

import com.g3d.util.G3DFormatter;
import com.g3d.audio.AudioRenderer;
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
        return true;
    }

    public static void setLowPermissions(boolean lowPerm){
        lowPermissions = lowPerm;
    }

    public static boolean isLowPermissions() {
        return lowPermissions;
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
        try{
            Class<? extends G3DContext> ctxClazz = null;
            switch (type){
                case Canvas:
                    ctxClazz = (Class<? extends G3DContext>) Class.forName("com.g3d.system.lwjgl.LwjglCanvas");
                    break;
                case Display:
                    ctxClazz = (Class<? extends G3DContext>) Class.forName("com.g3d.system.lwjgl.LwjglDisplay");
                    break;
                case OffscreenSurface:
                    ctxClazz = (Class<? extends G3DContext>) Class.forName("com.g3d.system.lwjgl.LwjglOffscreenBuffer");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported context type " + type);
            }

            return ctxClazz.newInstance();
        }catch (InstantiationException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (IllegalAccessException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (ClassNotFoundException ex){
            logger.log(Level.SEVERE, "CRITICAL ERROR: Context class is missing!\n" +
                                     "Make sure jme3_lwjgl-ogl is on the classpath.", ex);
        }
        
        return null;
    }

    public static G3DContext newContextJogl(AppSettings settings, G3DContext.Type type){
        try{
            Class<? extends G3DContext> ctxClazz = null;
            switch (type){
                case Display:
                    ctxClazz = (Class<? extends G3DContext>) Class.forName("com.g3d.system.jogl.JoglDisplay");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported context type " + type);
            }

            return ctxClazz.newInstance();
        }catch (InstantiationException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (IllegalAccessException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (ClassNotFoundException ex){
            logger.log(Level.SEVERE, "CRITICAL ERROR: Context class is missing!\n" +
                                     "Make sure jme3_jogl is on the classpath.", ex);
        }

        return null;
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
        Class<? extends AudioRenderer> clazz = null;
        try {
            if (settings.getAudioRenderer().startsWith("LWJGL")){
                clazz = (Class<? extends AudioRenderer>) Class.forName("com.g3d.audio.lwjgl.LwjglAudioRenderer");
            }else if (settings.getAudioRenderer().startsWith("JOAL")){
                clazz = (Class<? extends AudioRenderer>) Class.forName("com.g3d.audio.joal.JoalAudioRenderer");
            }else{
                throw new UnsupportedOperationException(
                                "Unrecognizable audio renderer specified: "+
                                settings.getAudioRenderer());
            }
            
            return clazz.newInstance();
        }catch (InstantiationException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (IllegalAccessException ex){
            logger.log(Level.SEVERE, "Failed to create context", ex);
        }catch (ClassNotFoundException ex){
            logger.log(Level.SEVERE, "CRITICAL ERROR: Audio implementation class is missing!\n" +
                                     "Make sure jme3_lwjgl-oal or jm3_joal is on the classpath.", ex);
        }
        return null;
    }

    public static void initialize(AppSettings settings){
        if (initialized)
            return;
        
        initialized = true;
        try {
            if (!lowPermissions){
                // can only modify logging settings
                // if permissions are available

                G3DFormatter formatter = new G3DFormatter();
                Handler fileHandler = new FileHandler("jme.log");
                fileHandler.setFormatter(formatter);
                Logger.getLogger("").addHandler(fileHandler);

                Handler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(formatter);
                Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
                Logger.getLogger("").addHandler(consoleHandler);

                Logger.getLogger("com.g3d").setLevel(Level.FINEST);
            }
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
