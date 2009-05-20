package com.g3d.app;

import com.g3d.input.Dispatcher;
import com.g3d.input.JoyInput;
import com.g3d.input.KeyInput;
import com.g3d.input.MouseInput;
import com.g3d.system.*;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.res.ContentManager;
import com.g3d.scene.SceneManager;
import com.g3d.system.DisplaySettings.Template;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The <code>Application</code> class represents an instance of a
 * real-time 3D rendering application. Typically the rendering is controlled
 * with a <code>SceneManager</code> implementation.
 */
public class Application implements Runnable {

    protected SceneManager sceneManager;
    protected ContentManager manager;

    protected Renderer renderer;
    protected G3DContext context;
    protected DisplaySettings settings;
    protected Timer timer;
    protected Camera cam;

    protected MouseInput mouseInput;
    protected KeyInput keyInput;
    protected JoyInput joyInput;
    protected Dispatcher dispatcher;

    protected AtomicBoolean needClose = new AtomicBoolean();

    /**
     * Create a new instance of <code>Application</code>.
     */
    public Application(){
        // Why initialize it here? 
        // Because it allows offline loading of content.
        initContentManager();
    }

    /**
     * Set the <code>SceneManager</code> to control rendering and updating
     * of the application's scene and real-time state respectively. It is
     * not recommended to change the SceneManager while the application
     * is running.
     * @param manager The scene manager to set
     */
    public void setSceneManager(SceneManager manager) {
        sceneManager = manager;
    }

    /**
     * Set the display settings to define the display created. Examples of
     * display parameters include display pixel width and height,
     * color bit depth, z-buffer bits, antialiasing samples, and update freqency.
     *
     * @param settings The settings to set.
     */
    public void setSettings(DisplaySettings settings){
        this.settings = settings;
    }

    /**
     * Initializes the display. If settings are not specified, a default
     * 640x480 display is created.
     */
    private void initDisplay(){
        context = G3DSystem.newDisplay();
        if (settings == null){
            settings = new DisplaySettings(Template.Default640x480);
        }
        context.setDisplaySettings(settings);

        // assuming display inited at this point
        context.create();
        settings = context.getDisplaySettings();

        timer = context.getTimer();
        renderer = context.getRenderer();

        assert renderer != null;
        assert context.isActive();
    }

    /**
     * Creates the camera to use for rendering. Default values are perspective
     * projection with 45° field of view, with near and far values 1 and 1000
     * units respectively.
     */
    private void initCamera(){
        cam = new Camera(settings.getWidth(), settings.getHeight());

        cam.setFrustumPerspective(45f, (float)cam.getWidth() / cam.getHeight(), 1f, 1000f);
        cam.setLocation(new Vector3f(0f, 0f, -10f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        renderer.setCamera(cam);
    }

    private void initInput(){
        mouseInput = context.getMouseInput();
        if (mouseInput != null)
            mouseInput.initialize();

        keyInput = context.getKeyInput();
        if (keyInput != null)
            keyInput.initialize();

        if (!settings.getBoolean("DisableJoysticks")){
            joyInput = context.getJoyInput();
            if (joyInput != null)
                joyInput.initialize();
        }
        // TODO: Write dummy implementation for joysticks

        dispatcher = new Dispatcher(mouseInput, keyInput, joyInput);
    }

    private void initContentManager(){
        manager = new ContentManager(true);
    }

    public ContentManager getContentManager(){
        return manager;
    }

    /**
     * @return the input binding event dispatcher.
     */
    public Dispatcher getDispatcher(){
        return dispatcher;
    }

    /**
     * @return The renderer for the application, or null if was not started yet.
     */
    public Renderer getRenderer(){
        return renderer;
    }

    /**
     * @return The display context for the application, or null if was not
     * started yet.
     */
    public G3DContext getContext(){
        return context;
    }

    /**
     * @return The camera for the application, or null if was not started yet.
     */
    public Camera getCamera(){
        return cam;
    }

    /**
     * Initializes the <code>Application</code>, by creating a display and
     * default camera. If display settings are not specified, a default
     * 640x480 display is created. Default values are used for the camera;
     * perspective projection with 45° field of view, with near
     * and far values 1 and 1000 units respectively.
     */
    protected void init(){
        initDisplay();
        initCamera();
        initInput();
    }

    protected void deinit(){
        if (mouseInput != null)
            mouseInput.destroy();

        if (keyInput != null)
            keyInput.destroy();

        if (joyInput != null)
            joyInput.destroy();

        timer.reset();
        renderer.cleanup();
        context.destroy();
        G3DSystem.destroy();
    }

    public void start(){
        new Thread(this, "G3D Render Thread").start();
    }

    public void stop(){
        needClose.set(true);
    }

    /**
     * Begins the main loop for the application, invoking the set
     * <code>SceneManager</code> for initialization first, then running
     * update and render calls each frame. Note that control will not be returned
     * to the calling code until the display is closed.
     */
    public void run(){
        init();

        if (sceneManager != null)
            sceneManager.init(renderer);

        // update timer so that the next delta is not too large
        timer.update();
        while (true){
            // update display.
            context.update();

            // close if needed
            if (context.isCloseRequested() || needClose.get()){
                break;
            }

            if (context.isActive()){
                // update timer
                timer.update();

                // update input
                if (mouseInput != null)
                    mouseInput.update();

                if (keyInput != null)
                    keyInput.update();

                if (joyInput != null)
                    joyInput.update();

                dispatcher.update(timer.getTimePerFrame());

                // clear display
                renderer.clearBuffers(true, true, true);

                // update logic and render
                if (sceneManager != null){
                    sceneManager.update(timer.getTimePerFrame());
                    sceneManager.render(renderer);
                }

                // flush the render queue to display all queued
                // objects.
                renderer.renderQueue();
            }
        }
        deinit();
    }

}
