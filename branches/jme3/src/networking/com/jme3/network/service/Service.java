package com.jme3.network.service;

/**
 * The Service interface. All services should implement this class, to provide a common way to manage service.
 *
 * @author Lars Wesselius
 */
public interface Service {
    /**
     * Start the service. This could mean starting threads, registering listeners, etc.
     */
    public void start();

    /**
     * Stop the service. Stop threads, unregister listeners, etc.
     */
    public void stop();

    /**
     * The connection type protocol that this service accepts. For example, a chat service would only
     *  allow TCP (most probably).
     *
     * @return The connection type enum.
     */
    public ConnectionProtocol acceptsConnectionProtocol();
}
