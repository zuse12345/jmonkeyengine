package com.g3d.asset;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <code>ImplHandler</code> manager the asset loader and asset locator
 * implementations in a thread safe way. This allows implementations
 * which store local persistent data to operate with a multi-threaded system.
 * This is done by keeping an instance of each content loader and content
 * locator object in a thread local.
 */
public class ImplHandler {

    private static final Logger logger = Logger.getLogger(ImplHandler.class.getName());

    private final AssetManager owner;
    
    private ImplThreadLocal genericLocator;

    private final Map<String, ImplThreadLocal> loaders =
                new Hashtable<String, ImplThreadLocal>();

    private final Map<String, ImplThreadLocal> locators =
                new Hashtable<String, ImplThreadLocal>();

    public ImplHandler(AssetManager owner){
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
     * @return The AssetInfo containing resource information required for
     * access, or null if not found.
     */
    public AssetInfo tryLocate(AssetKey key){
        ImplThreadLocal extLocal = null;
        ImplThreadLocal genericLocal = null;
        synchronized (locators){
            extLocal = locators.get(key.getExtension());
            genericLocal = genericLocator;
        }

        if (extLocal != null){
            AssetLocator locator = (AssetLocator) extLocal.get();
            if (extLocal.getExtraData() != null){
                locator.setRootPath((String) extLocal.getExtraData());
            }
            AssetInfo info = locator.locate(owner, key);
            if (info != null)
                return info;
        }
        if (genericLocal != null){
            AssetLocator locator = (AssetLocator) genericLocal.get();
            if (genericLocal.getExtraData() != null){
                locator.setRootPath((String) genericLocal.getExtraData());
            }
            AssetInfo info = locator.locate(owner, key);
            if (info != null)
                return info;
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
     * @return ContentLoader registered with registerLoader.
     */
    public AssetLoader aquireLoader(AssetKey key){
        synchronized (loaders){
            ImplThreadLocal local = loaders.get(key.getExtension());
            if (local != null){
                AssetLoader loader = (AssetLoader) local.get();
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
