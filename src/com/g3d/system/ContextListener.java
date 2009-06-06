package com.g3d.system;

/**
 * The <code>ContextListener> provides a means for an application
 * to recieve events relating to a context.
 */
public interface ContextListener {

    /**
     * Callback to indicate the application to initialize. This method
     * is called in the GL/Rendering thread so any GL-dependent resources
     * can be initialized.
     */
    public void initialize();

    /**
     * Callback to update the application state, and render the scene
     * to the back buffer.
     */
    public void update();

    /**
     * Callback to indicate that the context has been destroyed (either
     * by the user or requested by the application itself). Typically
     * cleanup of native resources should happen here. This method is called
     * in the GL/Rendering thread.
     */
    public void destroy();
}
