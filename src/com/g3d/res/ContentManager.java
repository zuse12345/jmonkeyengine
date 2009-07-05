package com.g3d.res;

import com.g3d.font.BitmapFont;
import com.g3d.font.BitmapFontLoader;
import com.g3d.material.J3MLoader;
import com.g3d.material.Material;
import com.g3d.material.MaterialDef;
import com.g3d.res.plugins.DDSLoader;
import com.g3d.res.plugins.AWTLoader;
import com.g3d.res.plugins.ClasspathLocator;
import com.g3d.res.plugins.GLSLLoader;
import com.g3d.res.plugins.HDRLoader;
import com.g3d.res.plugins.OBJLoader;
import com.g3d.res.plugins.TGALoader;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Spatial;
import com.g3d.shader.Shader;
import com.g3d.shader.ShaderMasterKey;
import com.g3d.system.G3DSystem;
import com.g3d.texture.Image;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentManager {

    private static final Logger logger = Logger.getLogger(ContentManager.class.getName());

    private final ContentCache cache = new ContentCache();
    private final ImplHandler handler = new ImplHandler(this);
    private final ThreadingManager threadingMan = new ThreadingManager(this);
    private final Set<ContentKey> alreadyLoadingSet = new HashSet<ContentKey>();
    private final Map<String, String> properties = new HashMap<String, String>();

    public ContentManager(){
        this(false);
    }

    public ContentManager(boolean loadDefaults){
        G3DSystem.initialize();
        if (loadDefaults){
            // enable flipping images by Y
//            setProperty("FlipImages", "true");

            //setup loading of resources from the classpath.
            registerLocator("/textures/",
                            ClasspathLocator.class,
                            "dds", "hdr", "tga",
                            "bmp", "png",
                            "jpg", "jpeg", "gif");
            registerLocator("/materials/", ClasspathLocator.class, "j3md", "j3m");
            registerLocator("/shaders/", ClasspathLocator.class, "glsl", "vert", "frag");
            registerLocator("/shaderlib/", ClasspathLocator.class, "glsllib");
            registerLocator("/models/", ClasspathLocator.class, "obj");
            registerLocator("/fonts/", ClasspathLocator.class, "fnt");
            registerLoader(AWTLoader.class, "jpg", "bmp", "gif", "png", "jpeg");
            registerLoader(J3MLoader.class, "j3m");
            registerLoader(J3MLoader.class, "j3md"); // use seperate loader
            registerLoader(BitmapFontLoader.class, "fnt");
            registerLoader(HDRLoader.class, "hdr");
            registerLoader(DDSLoader.class, "dds");
            registerLoader(TGALoader.class, "tga");
            registerLoader(OBJLoader.class, "obj");
            registerLoader(GLSLLoader.class, "vert", "frag", "glsl", "glsllib");
        }
        logger.info("ContentManager created.");
    }

    private String getExtension(String name){
        int idx = name.lastIndexOf('.');
        if (idx <= 0 || idx == name.length() - 1)
            return null;

        String ext = name.substring(idx+1).toLowerCase();
        return ext;
    }

    public void setProperty(String key, String value){
        properties.put(key, value);
    }

    public String getProperty(String key){
        return properties.get(key);
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

    /**
     * This method is thread-safe.
     * @param name
     * @return
     */
    public Object loadContent(ContentKey key, boolean useCache){
        String name = key.getName();
        Object o = useCache ? cache.getFromCache(key) : null;
        if (o == null){
            synchronized (alreadyLoadingSet){
                if (alreadyLoadingSet.contains(key)){
                    System.out.println("!!! Resource is already loading in another thread!");
                    return null;
                }
                alreadyLoadingSet.add(key);
            }

            String ext = getExtension(name);
            ContentLoader loader = handler.aquireLoader(ext);
            if (loader == null){
                logger.warning("No loader registered for"+
                               " type "+ext+".");
                return null;
            }

            if (handler.getLocatorCount() == 0){
                logger.warning("There are no locators currently"+
                               " registered. Use ContentManager."+
                               "registerLocator() to register a"+
                               " locator.");
                return null;
            }

            InputStream in = handler.tryLocate(name, ext);
            if (in == null){
                logger.warning("Cannot locate resource: "+name);
                return null;
            }
            
            try {
                o = loader.load(this, in, ext, key);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Failed to load resource: "+name, ex);
            }
            if (o == null){
                logger.warning("Error occured while loading resource "+name+
                               " using "+loader.getClass().getSimpleName());
            }else{
                logger.finer("Loaded resource "+name+" successfuly using "+
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

    public Object loadContent(ContentKey key){
        return loadContent(key, true);
    }

    public void loadContents(String ... names){
        for (String name : names){
            loadContent(new ContentKey(name));
        }
    }

    private Future<Object> loadContentLater(String name){
        return threadingMan.loadContent(name);
    }

    private Future<Void> loadContentsLater(String ... names){
        return threadingMan.loadContents(names);
    }

    public Texture loadTexture(String name){
        Image img = (Image) loadContent(new ContentKey(name));
        if (img == null)
            return null;

        Texture tex = new Texture2D(); // TODO: add support for 1D/3D/Cube images

        String genMips = getProperty("EnableMipmapGen");
        // enable mipmaps if image has them
        // or generate them if requested by user
        if (img.hasMipmaps() || (genMips != null && genMips.equals("true")))
            tex.setMinFilter(Texture.MinFilter.Trilinear);

        String aniso = getProperty("TexAnisoLevel");
        int anisoLevel = aniso != null ? Integer.parseInt(aniso) : 0;
        tex.setAnisotropicFilter(anisoLevel);

        tex.setName(name);
        tex.setImage(img);
        return tex;
    }

    public BitmapFont loadFont(String name){
        return (BitmapFont) loadContent(new ContentKey(name));
    }

    public Shader loadShader(ShaderMasterKey key){
        // cache abuse in method
        // that doesn't use loaders/locators
        Shader s = (Shader) cache.getFromCache(key);
        if (s == null){
            String vertName = key.getVertName();
            String fragName = key.getFragName();
            
            String vertSource = (String) loadContent(new ContentKey(vertName));
            String fragSource = (String) loadContent(new ContentKey(fragName));
            
            s = new Shader(key.getLanguage());
            s.addSource(Shader.ShaderType.Vertex, vertName, vertSource, key.getDefines().getCompiled());
            s.addSource(Shader.ShaderType.Fragment, fragName, fragSource, key.getDefines().getCompiled());

            cache.addToCache(key, s);
        }
        return s;
    }

    public Image loadImage(String name){
        return (Image) loadContent(new ContentKey(name));
    }

    public Spatial loadModel(String name){
        // TODO: Fix this when other loaders get added that return Spatial/Node.
        Mesh m = (Mesh) loadContent(new ContentKey(name));
        if (m == null)
            return null;
        
        Geometry g = new Geometry("Geometry: "+name, m);
        return g;
    }

    public MaterialDef loadMaterialDef(String name) {
        return (MaterialDef) loadContent(new ContentKey(name));
    }
    
    public Material loadMaterial(String name) {
        return (Material) loadContent(new ContentKey(name));
    }
}
