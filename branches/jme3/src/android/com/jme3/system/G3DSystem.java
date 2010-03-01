package com.g3d.system;

import android.content.res.Resources;
import com.g3d.asset.AndroidAssetManager;
import com.g3d.asset.AssetManager;
import com.g3d.system.G3DContext.Type;
import com.g3d.system.android.OGLESContext;
import com.g3d.util.AndroidLogHandler;
import com.g3d.util.G3DFormatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class G3DSystem {

    private static final Logger logger = Logger.getLogger(G3DSystem.class.getName());

    private static boolean initialized = false;
    private static Resources res;

    public static void initialize(AppSettings settings){
        if (initialized)
            return;

        initialized = true;
        try {
            G3DFormatter formatter = new G3DFormatter();

            Handler consoleHandler = new AndroidLogHandler();
            consoleHandler.setFormatter(formatter);
//            Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
//            Logger.getLogger("").addHandler(consoleHandler);

//            Logger.getLogger("com.g3d").setLevel(Level.FINEST);
        } catch (SecurityException ex){
            logger.log(Level.SEVERE, "Security error in creating log file", ex);
        }
        logger.info("Running on "+getFullName());
    }

    public static String getFullName(){
        return "jMonkey Engine 3 ALPHA 0.30";
    }
    
    public static G3DContext newContext(AppSettings settings, Type contextType) {
        initialize(settings);
        return new OGLESContext();
    }

    public static void setResources(Resources res){
        G3DSystem.res = res;
    }

    public static Resources getResources(){
        return res;
    }

    public static AssetManager newAssetManager(){
        return new AndroidAssetManager(true);
    }

}
