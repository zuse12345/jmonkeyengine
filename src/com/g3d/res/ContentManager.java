package com.g3d.res;

import com.g3d.material.J3MLoader;
import com.g3d.material.Material;
import com.g3d.material.MaterialDef;
import com.g3d.res.plugins.DDSLoader;
import com.g3d.res.plugins.AWTLoader;
import com.g3d.res.plugins.ClasspathLocator;
import com.g3d.res.plugins.HDRLoader;
import com.g3d.res.plugins.OBJLoader;
import com.g3d.res.plugins.TGALoader;
import com.g3d.res.plugins.TXTLoader;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Spatial;
import com.g3d.system.G3DSystem;
import com.g3d.texture.Image;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ContentManager {

    private static final Logger logger = Logger.getLogger(ContentManager.class.getName());

    private final ContentCache cache = new ContentCache();
    private final ImplHandler handler = new ImplHandler(this);
    private final ThreadingManager threadingMan = new ThreadingManager(this);
    private final Set<String> alreadyLoadingSet = new HashSet<String>();

    public ContentManager(){
        this(false);
    }

    public ContentManager(boolean loadDefaults){
        G3DSystem.initialize();
        if (loadDefaults){
            registerLocator("/textures/",
                            ClasspathLocator.class,
                            "dds", "hdr", "tga",
                            "bmp", "png",
                            "jpg", "jpeg", "gif");
            registerLocator("/materials/", ClasspathLocator.class, "j3md", "j3m");
            registerLocator("/shaders/", ClasspathLocator.class, "glsl", "vert", "frag");
            registerLocator("/models/", ClasspathLocator.class, "obj");
            registerLoader(AWTLoader.class, ImageIO.getReaderFileSuffixes());
            registerLoader(J3MLoader.class, "j3m");
            registerLoader(J3MLoader.class, "j3md"); // use seperate loader
            registerLoader(HDRLoader.class, "hdr");
            registerLoader(DDSLoader.class, "dds");
            registerLoader(TGALoader.class, "tga");
            registerLoader(OBJLoader.class, "obj");
            registerLoader(TXTLoader.class, "txt", "glsl", "vert", "frag");
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

    public void registerLoader(Class<?> loader, String ... extensions){
        handler.registerLoader(loader, extensions);
        if (logger.isLoggable(Level.FINER)){
            logger.finer("Registered loader: "+loader+" for extensions "+
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
            logger.finer("Registered locator: "+locator);
        }
    }

    /**
     * This method is thread-safe.
     * @param name
     * @return
     */
    public Object loadContent(String name){
        Object o = cache.getFromCache(name);
        if (o == null){
            synchronized (alreadyLoadingSet){
                if (alreadyLoadingSet.contains(name)){
                    System.out.println("!!! Resource is already loading in another thread!");
                    return null;
                }
                alreadyLoadingSet.add(name);
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
                o = loader.load(in, ext);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Failed to load resource: "+name, ex);
            }
            logger.finer("Loaded resource "+name+" successfuly using "+loader);
            cache.addToCache(name, o);

            synchronized (alreadyLoadingSet){
                if (!alreadyLoadingSet.contains(name)){
                    System.out.println("!!!! This really shouldn't happen!");
//                    return null;
                }
                alreadyLoadingSet.remove(name);
            }
            return o;
        }else{
            return o;
        }
    }

    public void loadContents(String ... names){
        for (String name : names){
            loadContent(name);
        }
    }

    private Future<Object> loadContentLater(String name){
        return threadingMan.loadContent(name);
    }

    private Future<Void> loadContentsLater(String ... names){
        return threadingMan.loadContents(names);
    }

    public Texture loadTexture(String name){
        Image img = (Image) loadContent(name);
        Texture tex = new Texture2D(); // add support for 1D/3D/Cube images
        tex.setImage(img);
        return tex;
    }

    public Image loadImage(String name){
        return (Image) loadContent(name);
    }

    public Spatial loadModel(String name){
        // TODO: Fix this when other loaders get added that return Spatial/Node.
        Mesh m = (Mesh) loadContent(name);
        if (m == null)
            return null;
        
        Geometry g = new Geometry("Geometry: "+name, m);
        return g;
    }

    public MaterialDef loadMaterialDef(String name) {
        return (MaterialDef) loadContent(name);
    }
    
    public Material loadMaterial(String name) {
        return (Material) loadContent(name);
    }
}
