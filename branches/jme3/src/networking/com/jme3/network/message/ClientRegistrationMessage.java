package com.jme3.network.message;

import com.jme3.network.serializing.Serializable;

/**
 * Client registration is a message that contains a unique ID. This ID
 *  is simply the current time in milliseconds, providing multiple clients
 *  will not connect to the same server within one millisecond. This is used
 *  to couple the TCP and UDP connections together into one 'Client' on the
 *  server.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class ClientRegistrationMessage extends Message {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
