package com.jme3.network.message;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Connection;
import com.jme3.network.serializing.Serializable;

/**
 * Message represents data being sent to the other side. This can be anything,
 *  and it will be serialized field by field. Extend this class if you wish to
 *  provide objects with common fields to the other side.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class Message {
    // The connector this message is meant for.
    private transient Client        connector;
    private transient Connection    connection;

    public Message(Connection connection) {
        this.connection = connection;
    }

    public Message() {}

    public Client getClient() {
        return connector;
    }

    public void setClient(Client connector) {
        this.connector = connector;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
