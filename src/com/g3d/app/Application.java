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
import com.g3d.system.AppSettings.Template;

/**
 * The <code>Application</code> class represents an instance of a
 * real-time 3D rendering application.
 */
public class Application implements ContextListener {

    /**
     * The content manager. Typically initialized outside the GL thread
     * to allow offline loading of content.
     */
    protected ContentManager manager;

    protected Renderer renderer;
    protected G3DContext context;
    protected AppSettings settings;
    protected Timer timer;
    protected Camera cam;

    protected boolean inputEnabled = true;
    protected MouseInput mouseInput;
    protected KeyInput keyInput;
    protected JoyInput joyInput;
    protected Dispatcher dispatcher;

    /**
     * Create a new instance of <code>Application</code>.
     */
    public Application(){
        // Why initialize it here? 
        // Because it allows offline loading of content.
        initContentManager();
    }

    /**
     * Set the display settings to define the display created. Examples of
     * display parameters include display pixel width and height,
     * color bit depth, z-buffer bits, antialiasing samples, and update freqency.
     *
     * @param settings The settings to set.
     */
    public void setSettings(AppSettings settings){
        this.settings = settings;
        if (context != null && settings.useInput() != inputEnabled){
            // may need to create or destroy input based
            // on settings change
            inputEnabled = !inputEnabled;
            if (inputEnabled){
                initInput();
            }else{
                destroyInput();
            }
        }else{
            inputEnabled = settings.useInput();
        }
    }

    private void initDisplay(){
        // aquire important objects
        // from the context
        settings = context.getSettings();
        timer = context.getTimer();
        renderer = context.getRenderer();
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

    /**
     * Initializes mouse and keyboard input. Also
     * initializes joystick input if joysticks are enabled in the
     * AppSettings.
     */
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

        dispatcher = new Dispatcher(mouseInput, keyInput, joyInput);
    }

    /**
     * Initializes the content manager.
     */
    private void initContentManager(){
        manager = new ContentManager(true);
    }

    /**
     * @return The content manager for this application.
     */
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
     * Starts the application. Creating a display and running the main loop.
     */
    public void start(){
        if (context != null && context.isCreated()){
            return;
        }

        if (settings == null){
            settings = new AppSettings(Template.Default640x480);
        }
        
        context = G3DSystem.newDisplay(settings);
        context.setContextListener(this);

        context.create();
    }

    /**
     * Requests the display to close, shutting down the main loop
     * and making neccessary cleanup operations.
     */
    public void stop(){
        context.destroy();
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     *
     * Initializes the <code>Application</code>, by creating a display and
     * default camera. If display settings are not specified, a default
     * 640x480 display is created. Default values are used for the camera;
     * perspective projection with 45° field of view, with near
     * and far values 1 and 1000 units respectively.
     */
    public void initialize(){
        initDisplay();
        initCamera();
        if (inputEnabled){
            initInput();
        }

        // update timer so that the next delta is not too large
        timer.update();

        // user code here..
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void update(){
        if (context.isCloseRequested()){
            context.destroy();
        }

        timer.update();

        if (inputEnabled){
            if (mouseInput != null)
                mouseInput.update();

            if (keyInput != null)
                keyInput.update();

            if (joyInput != null)
                joyInput.update();

            dispatcher.update(timer.getTimePerFrame());
        }

        // user code here..
    }

    protected void destroyInput(){
        if (mouseInput != null)
            mouseInput.destroy();

        if (keyInput != null)
            keyInput.destroy();

        if (joyInput != null)
            joyInput.destroy();

        dispatcher = null;
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void destroy(){
        destroyInput();
        timer.reset();
        renderer.cleanup();
        context.destroy();
        G3DSystem.destroy();
    }

}
