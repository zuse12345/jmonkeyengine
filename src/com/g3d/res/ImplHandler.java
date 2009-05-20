package com.g3d.res;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Makes sure the implementations of ContentLoader and ContentLocator
 * support multi-threading.
 * @author Kirill
 */
public class ImplHandler {

    private static final Logger logger = Logger.getLogger(ImplHandler.class.getName());

    private final ContentManager owner;
    
    private ImplThreadLocal genericLocator;

    private final Map<String, ImplThreadLocal> loaders =
                new Hashtable<String, ImplThreadLocal>();

    private final Map<String, ImplThreadLocal> locators =
                new Hashtable<String, ImplThreadLocal>();

    public ImplHandler(ContentManager owner){
        this.owner = owner;
    }

    protected class ImplThreadLocal extends ThreadLocal {

        private final Class<?> type;
        private final Object extraData;

        public ImplThreadLocal(Class<?> type){
            this.type = type;
            extraData = null;
        }

        public ImplThreadLocal(Class<?> type, Object extraData){
            this.type = type;
            this.extraData = extraData;
        }

        public Object getExtraData() {
            return extraData;
        }

        @Override
        protected Object initialValue(){
            try {
                return type.newInstance();
            } catch (InstantiationException ex) {
                logger.severe("Cannot create locator of type "+
                              type.getName()+", does the class"+
                              " have an empty and publically accessible"+
                              " constructor?");
                logger.throwing(type.getName(), "<init>", ex);
            } catch (IllegalAccessException ex) {
                logger.severe("Cannot create locator of type "+
                              type.getName()+", does the class"+
                              " have an empty and publically accessible"+
                              " constructor?");
                logger.throwing(type.getName(), "<init>", ex);
            }
            return null;
        }
    }

    /**
     * Attempts to locate the given resource name.
     * @param name The full name of the resource.
     * @return The InputStream containing the data of the resource, 
     * otherwise if not found, then null.
     */
    public InputStream tryLocate(String name, String ext){
        ImplThreadLocal extLocal = null;
        ImplThreadLocal genericLocal = null;
        synchronized (locators){
            extLocal = locators.get(ext);
            genericLocal = genericLocator;
        }

        if (extLocal != null){
            ContentLocator locator = (ContentLocator) extLocal.get();
            if (extLocal.getExtraData() != null){
                locator.setRootPath((String) extLocal.getExtraData());
            }
            InputStream stream = locator.locate(name);
            if (stream != null)
                return stream;
        }
        if (genericLocal != null){
            ContentLocator locator = (ContentLocator) genericLocal.get();
            if (genericLocal.getExtraData() != null){
                locator.setRootPath((String) genericLocal.getExtraData());
            }
            InputStream stream = locator.locate(name);
            if (stream != null)
                return stream;
        }
        return null;
    }

    public int getLocatorCount(){
        synchronized (locators){
            return locators.size();
        }
    }

    /**
     * Returns the ContentLoader registered for the given extension
     * of the current thread.
     * @param extension The lower case file extension, e.g "jpg".
     * @return ContentLoader registered with registerLoader.
     */
    public ContentLoader aquireLoader(String extension){
        synchronized (loaders){
            ImplThreadLocal local = loaders.get(extension);
            if (local != null){
                ContentLoader loader = (ContentLoader) local.get();
                loader.setOwner(owner);
                return loader;
            }
            return null;
        }
    }

    public void registerLoader(final Class<?> loaderType, String ... extensions){
        ImplThreadLocal local = new ImplThreadLocal(loaderType);
        for (String extension : extensions){
            extension = extension.toLowerCase();
            synchronized (loaders){
                loaders.put(extension, local);
            }
        }
    }

    public void registerLocator(final Class<?> locatorType, String rootPath, String ... extensions){
        ImplThreadLocal local = new ImplThreadLocal(locatorType, rootPath);
        if (extensions.length == 1 && extensions[0].equals("*")){
            synchronized (locators){
                genericLocator = local;
            }
        }else{
            for (String extension : extensions){
                extension = extension.toLowerCase();
                synchronized (locators){
                    locators.put(extension, local);
                }
            }
        }
    }

}
