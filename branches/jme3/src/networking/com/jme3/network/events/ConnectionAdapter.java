package com.jme3.network.events;

import com.jme3.network.connection.Client;

/**
 * Server adapter for making it easier to listen for server events.
 *
 * @author Lars Wesselius
 */
public class ConnectionAdapter implements ConnectionListener {
    public void clientConnected(Client client) {}
    public void clientDisconnected(Client client) {}
}
