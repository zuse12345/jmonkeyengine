package com.jme3.network.streaming;

import com.jme3.network.connection.Client;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.Message;
import com.jme3.network.message.StreamDataMessage;
import com.jme3.network.message.StreamMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientStreamingService extends MessageAdapter {
    private static Logger log = Logger.getLogger(StreamingService.class.getName());

    protected ArrayList<StreamListener>
                                streamListeners;

    protected ArrayList<Stream> streams;

    private Client client;


    public ClientStreamingService(Client client) {
        this.client = client;
        streams = new ArrayList<Stream>();
        streamListeners = new ArrayList<StreamListener>();
        client.addMessageListener(this, StreamDataMessage.class,
                                        StreamMessage.class);
    }

    // Client classes/methods //////////////////////////////////////////////////////////////

    public void addStreamListener(StreamListener listener) {
        streamListeners.add(listener);
    }

    public void removeStreamListener(StreamListener listener) {
        streamListeners.remove(listener);
    }

    public void messageReceived(Message message) {
        if (message instanceof StreamMessage && !(message instanceof StreamDataMessage)) {
            // A stream was offered.
            StreamMessage msg = (StreamMessage)message;
            Stream stream = getStream(msg.getStreamID());

            if (stream != null) {
                // This is a completion message.
                for (StreamListener listener : stream.getDataListeners()) {
                    listener.streamCompleted(msg);
                }
            } else {
                stream = new Stream();
                stream.setMessage(msg);
                boolean accept = fireStreamOffered(stream, msg);

                streams.add(stream);
                if (accept) {
                    try {
                        client.send(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (message instanceof StreamDataMessage) {
            StreamDataMessage dataMessage = (StreamDataMessage)message;
            Stream stream = getStream(dataMessage.getStreamID());
            if (stream == null) {
                log.log(Level.WARNING, "[StreamClient][TCP] We've received a data message even though we didn't register to the stream.");
                return;
            }

            for (StreamListener listener : stream.getDataListeners()) {
                listener.streamDataReceived(dataMessage);
            }
        }

    }

    private Stream getStream(short id) {
        for (Stream stream : streams) {
            if (stream.getMessage().getStreamID() == id) return stream;
        }
        return null;
    }

    private boolean fireStreamOffered(Stream stream, StreamMessage message) {
        boolean accept = false;
        for (StreamListener listener : streamListeners) {
            if (listener.streamOffered(message)) {
                accept = true;

                stream.addDataListener(listener);
            }
        }
        return accept;
    }
}
