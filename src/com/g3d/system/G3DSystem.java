package com.g3d.system;

import com.g3d.util.G3DFormatter;
import com.g3d.*;
import com.g3d.system.jogl.JoglDisplay;
import com.g3d.system.lwjgl.LwjglDisplay;
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
        return true;
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

    public static G3DContext newDisplay(AppSettings settings) {
        initialize();
        G3DContext ctx;
        if (settings.getRenderer().startsWith("LWJGL")){
            ctx = new LwjglDisplay();
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

    public static void initialize(){
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

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable thrown) {
                reportError("Uncaught exception thrown in "+thread.toString(), thrown);
            }
        });
        logger.fine("Running on thread: "+Thread.currentThread().getName());

        String val = System.getProperty("jnlp.g3d.nonativecopy");
        if (val == null || !val.equals("true")){
            try {
                Natives.extractNativeLibs(getPlatformID());
            } catch (IOException ex) {
                reportError("Error while copying native libraries", ex);
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
        return "jMonkeyEngine 0.16";
    }

    public static void reportError(String errorMsg){
        reportError(errorMsg, null);
    }

    public static void reportError(String errorMsg, Throwable thrown){
        logger.log(Level.SEVERE, errorMsg, thrown);
        JOptionPane.showMessageDialog(null, errorMsg + "\n" + thrown.toString(), getFullName(), JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

}
