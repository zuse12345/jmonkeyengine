package com.g3d.asset;

import com.g3d.audio.AudioData;
import com.g3d.font.BitmapFont;
import com.g3d.font.plugins.BitmapFontLoader;
import com.g3d.material.plugins.J3MLoader;
import com.g3d.material.Material;
import com.g3d.material.MaterialDef;
import com.g3d.texture.plugins.DDSLoader;
import com.g3d.texture.plugins.AWTLoader;
import com.g3d.asset.plugins.ClasspathLocator;
import com.g3d.shader.GLSLLoader;
import com.g3d.texture.plugins.HDRLoader;
import com.g3d.audio.plugins.JOGGLoader;
import com.g3d.scene.plugins.OBJLoader;
import com.g3d.texture.plugins.PFMLoader;
import com.g3d.texture.plugins.TGALoader;
import com.g3d.audio.plugins.WAVLoader;
import com.g3d.scene.Spatial;
import com.g3d.scene.plugins.ogre.*;
import com.g3d.shader.Shader;
import com.g3d.shader.ShaderKey;
import com.g3d.system.G3DSystem;
import com.g3d.texture.Image;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import com.g3d.texture.TextureCubeMap;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        G3DSystem.initialize();

        if (loadDefaults){
            //setup loading of resources from the classpath.
            registerLocator("/textures/",
                            ClasspathLocator.class,
                            "dds", "hdr", "pfm", "tga",
                            "bmp", "png",
                            "jpg", "jpeg", "gif");
            registerLocator("/sounds/", ClasspathLocator.class, "wav", "ogg", "spx");
            registerLocator("/materials/", ClasspathLocator.class, "j3md", "j3m");
            registerLocator("/shaders/", ClasspathLocator.class, "glsl", "vert", "frag");
            registerLocator("/shaderlib/", ClasspathLocator.class, "glsllib");
            registerLocator("/models/", ClasspathLocator.class, "obj", "meshxml", "material");
            registerLocator("/fonts/", ClasspathLocator.class, "fnt");
            registerLoader(AWTLoader.class, "jpg", "bmp", "gif", "png", "jpeg");
            registerLoader(WAVLoader.class, "wav");
            registerLoader(JOGGLoader.class, "ogg");
            registerLoader(J3MLoader.class, "j3m");
            registerLoader(J3MLoader.class, "j3md"); // use seperate loader
            registerLoader(BitmapFontLoader.class, "fnt");
            registerLoader(PFMLoader.class, "pfm");
            registerLoader(HDRLoader.class, "hdr");
            registerLoader(DDSLoader.class, "dds");
            registerLoader(TGALoader.class, "tga");
            registerLoader(OBJLoader.class, "obj");
            registerLoader(MeshLoader.class, "meshxml");
            registerLoader(MaterialLoader.class, "material");
            registerLoader(SceneLoader.class, "scene");
            registerLoader(GLSLLoader.class, "vert", "frag", "glsl", "glsllib");
        }
        logger.info("ContentManager created.");
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

    public void clearCache(){
        cache.deleteAllAssets();
    }

    /**
     * This method is thread-safe.
     * @param name
     * @return
     */
    public Object loadContent(AssetKey key, boolean useCache){
        Object o = useCache ? cache.getFromCache(key) : null;
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
                logger.finer("Loaded resource "+key+" successfuly using "+
                             loader.getClass().getSimpleName());

                if (useCache)
                    cache.addToCache(key, o);
            }

            synchronized (alreadyLoadingSet){
                if (!alreadyLoadingSet.contains(key)){
                    System.out.println("!!!! This really shouldn't happen!");
//                    return null;
                }
                alreadyLoadingSet.remove(key);
            }
            return o;
        }else{
            return o;
        }
    }

    public Object loadContent(AssetKey key){
        return loadContent(key, true);
    }

    public void loadContents(String ... names){
        for (String name : names){
            loadContent(new AssetKey(name));
        }
    }

    private Future<Object> loadContentLater(String name){
        return threadingMan.loadContent(name);
    }

    private Future<Void> loadContentsLater(String ... names){
        return threadingMan.loadContents(names);
    }

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
    public Texture loadTexture(String name, boolean generateMipmaps, boolean flipY, boolean asCube, int anisotropy){
        Image img = (Image) loadContent(new TextureKey(name, flipY));
        if (img == null)
            return null;

        Texture tex;
        if (asCube){
            if (flipY){
                // also flip -y and +y image in cubemap
                ByteBuffer pos_y = img.getData(2);
                img.setData(2, img.getData(3));
                img.setData(3, pos_y);
            }
            tex = new TextureCubeMap();
        }else{
            tex = new Texture2D();
        }

        // enable mipmaps if image has them
        // or generate them if requested by user
        if (img.hasMipmaps() || generateMipmaps)
            tex.setMinFilter(Texture.MinFilter.Trilinear);

        tex.setAnisotropicFilter(anisotropy);
        tex.setName(name);
        tex.setImage(img);
        return tex;
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
        return loadTexture(name, generateMipmaps, true, false, Integer.MAX_VALUE);
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
        return (BitmapFont) loadContent(new TextureKey(name, true));
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
     * Load a Ogre model given a matFileName.
     *
     * @param name
     * @param matFileName
     * @return
     */
    public Spatial loadOgreModel(String name, String matFileName)
    {
        OgreMaterialList materialList = (OgreMaterialList) loadContent(new AssetKey(matFileName));
        return (Spatial) loadContent(new OgreMeshKey(name, materialList));
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

    /**
     * Load an audio file.
     *
     * @param name
     * @param stream If true, will be loaded as AudioStream, otherwise
     * as AudioBuffer.
     * @return
     */
    public AudioData loadAudio(String name, boolean stream){
        return (AudioData) loadContent(new AudioKey(name, stream), !stream);
    }

    /**
     * Load a material definition file (J3MD).
     * @param name
     * @return
     */
    public MaterialDef loadMaterialDef(String name) {
        return (MaterialDef) loadContent(new AssetKey(name));
    }

    /**
     * Load a material file (J3M).
     *
     * @param name
     * @return
     */
    public Material loadMaterial(String name) {
        return (Material) loadContent(new AssetKey(name));
    }
}
