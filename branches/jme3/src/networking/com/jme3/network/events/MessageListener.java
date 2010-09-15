package com.jme3.network.events;

import com.jme3.network.message.Message;

/**
 * Listener for messages.
 *
 * @author Lars Wesselius
 */
public interface MessageListener {
    public void messageReceived(Message message);
    public void messageSent(Message message);

    public void objectReceived(Object object);
    public void objectSent(Object object);
}
