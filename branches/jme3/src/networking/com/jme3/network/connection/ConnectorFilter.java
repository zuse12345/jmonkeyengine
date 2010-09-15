package com.jme3.network.connection;

import java.net.InetSocketAddress;

/**
 * A connection filter that can be used by the dev to filter
 *  connections based on <code>InetAddress</code>es.
 *
 * @author Lars Wesselius
 */
public interface ConnectorFilter {

    /**
     * Filter a connection based on <code>InetAddress</code>. This is called
     *  everytime a client, or <code>Client</code>, connects to the server.
     *
     * @param address The address.
     * @return A null string if the connection should be accepted without problems.
     *         A non null value indicates the reason of why the client should be dropped.
     */
    public String filterConnector(InetSocketAddress address);
}
