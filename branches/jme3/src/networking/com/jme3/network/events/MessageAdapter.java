package com.jme3.network.events;

import com.jme3.network.message.Message;

/**
 * Message adapter to make it easier to listen to message vents.
 *
 * @author Lars Wesselius
 */
public class MessageAdapter implements MessageListener {
    public void messageReceived(Message message) {}
    public void messageSent(Message message) {}
    public void objectReceived(Object object) {}
    public void objectSent(Object object) {}
}
