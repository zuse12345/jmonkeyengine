package com.g3d.asset;

import com.g3d.font.BitmapFont;
import com.g3d.material.Material;
import com.g3d.scene.Spatial;
import com.g3d.shader.Shader;
import com.g3d.shader.ShaderKey;
import com.g3d.texture.Texture;
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
public class AssetManager {

    private static final Logger logger = Logger.getLogger(AssetManager.class.getName());

    private final AssetCache cache = new AssetCache();
    private final ImplHandler handler = new ImplHandler(this);
    private final ThreadingManager threadingMan = new ThreadingManager(this);
    private final Set<AssetKey> alreadyLoadingSet = new HashSet<AssetKey>();

    public AssetManager(){
        this(false);
    }

    public AssetManager(boolean loadDefaults){
        if (loadDefaults){
            //setup loading of resources from the classpath.
            //            registerLocator("/textures/",
            //                            ClasspathLocator.class,
            //                            "dds", "hdr", "pfm", "tga",
            //                            "bmp", "png",
            //                            "jpg", "jpeg", "gif");
            //            registerLocator("/sounds/", ClasspathLocator.class, "wav", "ogg", "spx");
            //            registerLocator("/materials/", ClasspathLocator.class, "j3md", "j3m");
            //            registerLocator("/shaders/", ClasspathLocator.class, "glsl", "vert", "frag");
            //            registerLocator("/shaderlib/", ClasspathLocator.class, "glsllib");
            //            registerLocator("/models/", ClasspathLocator.class, "j3o", "obj", "meshxml", "skeletonxml", "material");
            //            registerLocator("/fonts/", ClasspathLocator.class, "fnt");
            //            registerLocator("/", ClasspathLocator.class, "*");
            //            registerLoader(AWTLoader.class, "jpg", "bmp", "gif", "png", "jpeg");
            //            registerLoader(WAVLoader.class, "wav");
            //            registerLoader(JOGGLoader.class, "ogg");
            //            registerLoader(J3MLoader.class, "j3m");
            //            registerLoader(J3MLoader.class, "j3md"); // use seperate loader
            //            registerLoader(BitmapFontLoader.class, "fnt");
            //            registerLoader(PFMLoader.class, "pfm");
            //            registerLoader(HDRLoader.class, "hdr");
            //            registerLoader(DDSLoader.class, "dds");
            //            registerLoader(TGALoader.class, "tga");
            //            registerLoader(BinaryImporter.class, "j3o");
            //            registerLoader(OBJLoader.class, "obj");
            //            registerLoader(MeshLoader.class, "meshxml");
            //            registerLoader(SkeletonLoader.class, "skeletonxml");
            //            registerLoader(MaterialLoader.class, "material");
            //            registerLoader(SceneLoader.class, "scene");
            //            registerLoader(GLSLLoader.class, "vert", "frag", "glsl", "glsllib");

            AssetConfig cfg = new AssetConfig(this);
            InputStream stream = AssetManager.class.getResourceAsStream("Desktop.cfg");
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
        logger.info("ContentManager created.");
    }

    public void registerLoader(String clsName, String ... extensions){
        Class<? extends AssetLoader> clazz = null;
        try{
            clazz = (Class<? extends AssetLoader>) Class.forName(clsName);
        }catch (ClassNotFoundException ex){
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
        }
        if (clazz != null){
            registerLocator(rootPath, clazz, extensions);
        }
    }

    public void registerLoader(Class<?> loader, String ... extensions){
        handler.registerLoader(loader, extensions);
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
        handler.registerLocator(locator, rootPath, extensions);
        if (logger.isLoggable(Level.FINER)){
            logger.finer("Registered locator: "+locator.getSimpleName());
        }
    }
    
    public void unregisterLocator(String rootPath, Class<?> locator, String ... extensions){
        handler.unregisterLocator(locator, rootPath, extensions);
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
    public Object loadContent(AssetKey key){
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

    public Object loadContent(String name){
        return loadContent(new AssetKey(name));
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
        return (Texture) loadContent(key);
    }

    public Material loadMaterial(String name){
        return (Material) loadContent(new AssetKey(name));
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
        key.setAnisotropy(Integer.MAX_VALUE);
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

    /**
     * Loads a bitmap font with the given name.
     *
     * @param name
     * @return
     */
    public BitmapFont loadFont(String name){
        return (BitmapFont) loadContent(new AssetKey(name));
    }

    public InputStream loadGLSLLibrary(AssetKey key){
        return (InputStream) loadContent(key);
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

            String vertSource = (String) loadContent(new AssetKey(vertName));
            String fragSource = (String) loadContent(new AssetKey(fragName));

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
        return (Spatial) loadContent(new AssetKey(name));
    }
    
}
