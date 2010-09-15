package com.jme3.network.events;

import com.jme3.network.connection.Client;

/**
 * Listener for server events.
 *
 * @author Lars Wesselius
 */
public interface ConnectionListener {
    public void clientConnected(Client client);
    public void clientDisconnected(Client client);
}
