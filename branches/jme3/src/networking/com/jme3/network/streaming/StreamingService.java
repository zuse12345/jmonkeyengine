package com.jme3.network.streaming;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.StreamMessage;
import com.jme3.network.service.Service;

import java.io.InputStream;

/**
 * Streaming service handles all kinds of streaming to clients. It can be instantiated by
 *  both the client and server, where server will work as sender, and client as receiver.
 *
 * @author Lars Wesselius
 */
public class StreamingService extends MessageAdapter implements Service {

    private ClientStreamingService clientService;
    private ServerStreamingService serverService;

    public StreamingService(Client client) {
        clientService = new ClientStreamingService(client);
    }

    public StreamingService(Server server) {
        serverService = new ServerStreamingService(server);
    }

    public void offerStream(Client client, StreamMessage msg, InputStream data) {
        if (serverService == null) return;
        serverService.offerStream(client, msg, data);
    }

    public void addStreamListener(StreamListener listener) {
        if (clientService == null) return;
        clientService.addStreamListener(listener);
    }

    public void removeStreamListener(StreamListener listener) {
        if (clientService == null) return;
        clientService.removeStreamListener(listener);
    }
}
