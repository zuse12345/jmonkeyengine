package g3dtools.deploy.conv;

import com.g3d.asset.AssetManager;
import com.g3d.renderer.Renderer;
import com.g3d.system.AppSettings;
import com.g3d.system.G3DContext;
import com.g3d.system.G3DContext.Type;
import com.g3d.system.G3DSystem;
import com.g3d.system.SystemListener;
import com.g3d.texture.Texture2D;

public class LwjglCompressor implements SystemListener {

    private static Texture2D texture;
    private static G3DContext context;

    public static void main(String[] args){
        AssetManager manager = new AssetManager(true);
        texture = (Texture2D) manager.loadTexture("pond1.png", true, false, false, 0);
        
        AppSettings cfg = new AppSettings(true);
        cfg.setAudioRenderer(null);

        cfg.setRenderer(AppSettings.LWJGL_OPENGL2);
        cfg.setFullscreen(false);
        cfg.setResolution(1, 1);
        cfg.setBitsPerPixel(32);
        cfg.setSamples(0);

        cfg.setUseInput(false);
        cfg.setUseJoysticks(false);

        context = G3DSystem.newContext(cfg, Type.OffscreenSurface);
        context.setAutoFlushFrames(false);
        context.setSystemListener(new LwjglCompressor());
        context.create();
    }

    public void initialize() {
        Renderer r = context.getRenderer();
        r.setTexture(0, texture);
    }

    public void reshape(int width, int height) {
    }

    public void update() {
    }

    public void requestClose(boolean esc) {
    }

    public void gainFocus() {
    }

    public void loseFocus() {
    }

    public void handleError(String errorMsg, Throwable t) {
    }

    public void destroy() {
    }

}
