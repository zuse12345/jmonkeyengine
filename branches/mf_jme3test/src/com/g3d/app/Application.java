package com.g3d.app;

import com.g3d.input.JoyInput;
import com.g3d.input.KeyInput;
import com.g3d.input.MouseInput;
import com.g3d.system.*;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue;
import com.g3d.asset.AssetManager;
import com.g3d.input.InputManager;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial;
import java.util.List;
import java.util.logging.Logger;

/**
 * The <code>Application</code> class represents an instance of a
 * real-time 3D rendering application.
 */
public class Application implements SystemListener {

    private static final Logger logger = Logger.getLogger(Application.class.getName());

    /**
     * The content manager. Typically initialized outside the GL thread
     * to allow offline loading of content.
     */
    protected AssetManager manager;

    protected Renderer renderer;
    protected G3DContext context;
    protected AppSettings settings;
    protected Timer timer;
    protected Camera cam;

    protected boolean inputEnabled = true;
    protected float speed = 1f;
    protected MouseInput mouseInput;
    protected KeyInput keyInput;
    protected JoyInput joyInput;
    protected InputManager inputManager;

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
        cam.setLocation(new Vector3f(0f, 0f, 10f));
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

        inputManager = new InputManager(mouseInput, keyInput, joyInput);
    }

    /**
     * Initializes the content manager.
     */
    private void initContentManager(){
        manager = new AssetManager(true);
    }

    /**
     * @return The content manager for this application.
     */
    public AssetManager getContentManager(){
        return manager;
    }

    /**
     * @return the input manager.
     */
    public InputManager getInputManager(){
        return inputManager;
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

    private void renderShadow(Spatial s, Renderer r){
        if (s instanceof Node){
            Node n = (Node) s;
            List<Spatial> children = n.getChildren();
            for (int i = 0; i < children.size(); i++){
                renderShadow(children.get(i), r);
            }
        }else if (s instanceof Geometry){
            Geometry gm = (Geometry) s;
            RenderQueue.ShadowMode shadowMode = s.getShadowMode();
            if (shadowMode != RenderQueue.ShadowMode.Off)
                r.addToShadowQueue(gm, shadowMode);
        }
    }

    public void render(Spatial s, Renderer r){
        if (!s.checkCulling(cam)){
            // move on to shadow-only render
            if (s.getShadowMode() != RenderQueue.ShadowMode.Off)
                renderShadow(s,r);

            return;
        }
        if (s instanceof Node){
            Node n = (Node) s;
            List<Spatial> children = n.getChildren();
            for (int i = 0; i < children.size(); i++){
                render(children.get(i), r);
            }
        }else if (s instanceof Geometry){
            Geometry gm = (Geometry) s;
            r.addToQueue(gm, s.getQueueBucket());

            RenderQueue.ShadowMode shadowMode = s.getShadowMode();
            if (shadowMode != RenderQueue.ShadowMode.Off)
                r.addToShadowQueue(gm, shadowMode);
        }
    }

    /**
     * Starts the application. Creating a display and running the main loop.
     */
    public void start(){
        if (context != null && context.isCreated()){
            logger.warning("start() called when application already created!");
            return;
        }

        if (settings == null){
            settings = new AppSettings(true);
        }
        
        logger.fine("Starting application: "+getClass().getName());
        context = G3DSystem.newDisplay(settings);
        context.setSystemListener(this);
        context.create();
    }

    public void restart(){
        context.restart(true);
    }

    /**
     * Requests the display to close, shutting down the main loop
     * and making neccessary cleanup operations.
     */
    public void stop(){
        logger.fine("Closing application: "+getClass().getName());
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

    public void handleError(String errMsg, Throwable t){

    }

    public void gainFocus(){
        speed = 1;
        System.out.println("gainFocus");
    }

    public void loseFocus(){
        speed = 0;
        System.out.println("loseFocus");
    }

    public void requestClose(boolean esc){
        context.destroy();
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void update(){
        if (speed == 0)
            return;

        timer.update();

        if (inputEnabled){
            if (mouseInput != null)
                mouseInput.update();

            if (keyInput != null)
                keyInput.update();

            if (joyInput != null)
                joyInput.update();

            inputManager.update(timer.getTimePerFrame());
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

        inputManager = null;
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void destroy(){
        destroyInput();
        timer.reset();
        renderer.cleanup();
        // NOTE: May cause issues with programs
        // that use multiple Application instances
//        G3DSystem.destroy();
    }

}
