package com.jme3.asset;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.TextureKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetConfig;
import com.jme3.audio.AudioData;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.texture.Texture;
import com.jme3.texture.plugins.AWTLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>AssetManager</code> is the primary method for managing and loading
 * resources inside jME.
 *
 * @author Kirill Vainer
 */
public class DesktopAssetManager implements AssetManager {

    private static final Logger logger = Logger.getLogger(AssetManager.class.getName());

    private final AssetCache cache = new AssetCache();
    private final ImplHandler handler = new ImplHandler(this);
    private final ThreadingManager threadingMan = new ThreadingManager(this);
    private final Set<AssetKey> alreadyLoadingSet = new HashSet<AssetKey>();

    public DesktopAssetManager(){
        this(false);
    }

    public DesktopAssetManager(boolean loadDefaults){
        if (loadDefaults){
            AssetConfig cfg = new AssetConfig(this);
            InputStream stream = DesktopAssetManager.class.getResourceAsStream("Desktop.cfg");
            try{
                cfg.loadText(stream);
            }catch (IOException ex){
                logger.log(Level.SEVERE, "Failed to load asset config", ex);
            }finally{
                if (stream != null)
                    try{
                        stream.close();
                    }catch (IOException ex){
                    }
            }
        }
        logger.info("DesktopAssetManager created.");
    }

    public void registerLoader(String clsName, String ... extensions){
        Class<? extends AssetLoader> clazz = null;
        try{
            clazz = (Class<? extends AssetLoader>) Class.forName(clsName);
        }catch (ClassNotFoundException ex){
            logger.log(Level.WARNING, "Failed to find loader: "+clsName, ex);
        }
        if (clazz != null){
            registerLoader(clazz, extensions);
        }
    }

    public void registerLocator(String rootPath, String clsName, String ... extensions){
        Class<? extends AssetLoader> clazz = null;
        try{
            clazz = (Class<? extends AssetLoader>) Class.forName(clsName);
        }catch (ClassNotFoundException ex){
            logger.log(Level.WARNING, "Failed to find locator: "+clsName, ex);
        }
        if (clazz != null){
            registerLocator(rootPath, clazz, extensions);
        }
    }

    public void registerLoader(Class<?> loader, String ... extensions){
        handler.addLoader(loader, extensions);
        if (logger.isLoggable(Level.FINER)){
            logger.finer("Registered loader: "+loader.getSimpleName()+" for extensions "+
                        Arrays.toString(extensions));
        }
    }

    public void registerLocator(Class<?> locator){
        registerLocator(locator, "*");
    }

    public void registerLocator(Class<?> locator, String ... extensions){
        registerLocator(null, locator, extensions);
    }

    public void registerLocator(String rootPath, Class<?> locator, String ... extensions){
        handler.addLocator(locator, rootPath, extensions);
        if (logger.isLoggable(Level.FINER)){
            logger.finer("Registered locator: "+locator.getSimpleName());
        }
    }
    
    public void unregisterLocator(String rootPath, Class<?> locator, String ... extensions){
        handler.removeLocator(locator, rootPath, extensions);
        if (logger.isLoggable(Level.FINER)){
            logger.finer("Unregistered locator: "+locator.getSimpleName());
        }
    }

    public void clearCache(){
        cache.deleteAllAssets();
    }

    public void addToCache(AssetKey key, Object asset){
        cache.addToCache(key, asset);
    }

    /**
     * This method is thread-safe.
     * @param name
     * @return
     */
    public Object loadAsset(AssetKey key){
        Object o = key.shouldCache() ? cache.getFromCache(key) : null;
        if (o == null){
            synchronized (alreadyLoadingSet){
                if (alreadyLoadingSet.contains(key)){
                    System.out.println("!!! Resource is already loading in another thread!");
                    return null;
                }
                alreadyLoadingSet.add(key);
            }

            AssetLoader loader = handler.aquireLoader(key);
            if (loader == null){
                logger.warning("No loader registered for"+
                               " type "+key.getExtension()+".");
                return null;
            }

            if (handler.getLocatorCount() == 0){
                logger.warning("There are no locators currently"+
                               " registered. Use ContentManager."+
                               "registerLocator() to register a"+
                               " locator.");
                return null;
            }

            AssetInfo info = handler.tryLocate(key);
            if (info == null){
                logger.warning("Cannot locate resource: "+key);
                return null;
            }

            try {
                o = loader.load(info);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Failed to load resource: "+key, ex);
            }
            if (o == null){
                logger.warning("Error occured while loading resource "+key+
                               " using "+loader.getClass().getSimpleName());
            }else{
                logger.finer("Loaded "+key+" with "+
                             loader.getClass().getSimpleName());

                // do processing on asset before caching
                o = key.postProcess(o);

                if (key.shouldCache())
                    cache.addToCache(key, o);
            }

            synchronized (alreadyLoadingSet){
                if (!alreadyLoadingSet.contains(key)){
                    System.out.println("!!!! This really shouldn't happen!");
//                    return null;
                }
                alreadyLoadingSet.remove(key);
            }
        }

        // object o is the asset
        // create an instance for user
        return key.createClonedInstance(o);
    }

    public Object loadAsset(String name){
        return loadAsset(new AssetKey(name));
    }

//    public void loadContents(String ... names){
//        for (String name : names){
//            loadContent(new AssetKey(name));
//        }
//    }

//    private Future<Object> loadContentLater(String name){
//        return threadingMan.loadContent(name);
//    }
//
//    private Future<Void> loadContentsLater(String ... names){
//        return threadingMan.loadContents(names);
//    }

    /**
     * Loads a texture.
     *
     * @param name Name of the texture.
     * @param generateMipmaps Enable if applying texture to 3D objects, disable
     * for GUI/HUD elements.
     * @param flipY If to flip image along Y axis- turn on for LWJGL or JOGL
     * renderer.
     * @param anisotropy Anisotropic filter value. Set to Integer.MAX_VALUE to
     * select highest supported by video card.
     * @return
     */
    public Texture loadTexture(TextureKey key){
        return (Texture) loadAsset(key);
    }

    public Material loadMaterial(String name){
        return (Material) loadAsset(new AssetKey(name));
    }

    /**
     * Loads a texture.
     *
     * @param name
     * @param generateMipmaps Enable if applying texture to 3D objects, disable
     * for GUI/HUD elements.
     * @return
     */
    public Texture loadTexture(String name, boolean generateMipmaps){
        TextureKey key = new TextureKey(name, true);
        key.setGenerateMips(generateMipmaps);
        key.setAsCube(false);
        return loadTexture(key);
    }

    public Texture loadTexture(String name, boolean generateMipmaps, boolean flipY, boolean asCube, int aniso){
        TextureKey key = new TextureKey(name, flipY);
        key.setGenerateMips(generateMipmaps);
        key.setAsCube(asCube);
        key.setAnisotropy(aniso);
        return loadTexture(key);
    }

    public Texture loadTexture(String name){
        return loadTexture(name, true);
    }

    public AudioData loadAudio(AudioKey key){
        return (AudioData) loadAsset(key);
    }

    public AudioData loadAudio(String name){
        return loadAudio(new AudioKey(name, false));
    }

    /**
     * Loads a bitmap font with the given name.
     *
     * @param name
     * @return
     */
    public BitmapFont loadFont(String name){
        return (BitmapFont) loadAsset(new AssetKey(name));
    }

    public InputStream loadGLSLLibrary(AssetKey key){
        return (InputStream) loadAsset(key);
    }

    /**
     * Load a vertex/fragment shader combo.
     *
     * @param key
     * @return
     */
    public Shader loadShader(ShaderKey key){
        // cache abuse in method
        // that doesn't use loaders/locators
        Shader s = (Shader) cache.getFromCache(key);
        if (s == null){
            String vertName = key.getVertName();
            String fragName = key.getFragName();

            String vertSource = (String) loadAsset(new AssetKey(vertName));
            String fragSource = (String) loadAsset(new AssetKey(fragName));

            s = new Shader(key.getLanguage());
            s.addSource(Shader.ShaderType.Vertex,   vertName, vertSource, key.getDefines().getCompiled());
            s.addSource(Shader.ShaderType.Fragment, fragName, fragSource, key.getDefines().getCompiled());

            cache.addToCache(key, s);
        }
        return s;
    }

    /**
     * Load a model.
     *
     * @param name
     * @return
     */
    public Spatial loadModel(String name){
        return (Spatial) loadAsset(new AssetKey(name));
    }
    
}
