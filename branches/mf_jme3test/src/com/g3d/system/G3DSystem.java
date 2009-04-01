package com.g3d.system;

import com.g3d.util.G3DFormatter;
import com.g3d.*;
import com.g3d.system.lwjgl.LwjglDisplay;
import com.g3d.util.Natives;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static G3DContext newDisplay() {
        initialize();
        return new LwjglDisplay();
    }

    public static void initialize(){
        if (initialized)
            return;

        initialized = true;
        try {
            Handler fileHandler = new FileHandler("gorilla3d.log");
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
            @Override
            public void uncaughtException(Thread thread, Throwable thrown) {
                reportError("Uncaught exception thrown in "+thread.toString(), thrown);
            }
        });
        logger.fine("Running on thread: "+Thread.currentThread().getName());
        
        try {
            Natives.extractNativeLibs();
        } catch (IOException ex) {
            reportError("Error while copying native libraries", ex);
        }
    }

    public static void destroy() {
        if (!initialized)
            return;

        initialized = false;
        logger.finer(getFullName() + " closing.");
    }

    public static String getFullName(){
        return "Gorilla3D Engine 0.03";
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
