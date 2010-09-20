package com.jme3.network.streaming;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.Message;
import com.jme3.network.message.StreamDataMessage;
import com.jme3.network.message.StreamMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerStreamingService extends MessageAdapter {
    private static Logger log = Logger.getLogger(StreamingService.class.getName());

    protected ArrayList<Stream> streams;

    private short nextStreamID = Short.MIN_VALUE;

    public ServerStreamingService(Server server) {
        streams = new ArrayList<Stream>();
        server.addMessageListener(this, StreamMessage.class);
    }

    public void offerStream(Client client, StreamMessage msg, InputStream data) {
        short streamID = ++nextStreamID;
        msg.setStreamID(streamID);
        msg.setReliable(true);

        Stream stream = new Stream();
        stream.setData(data);
        stream.setMessage(msg);
        stream.setReceiver(client);
        streams.add(stream);

        try {
            client.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startStream(Stream stream) {
        Client receiver = stream.getReceiver();
        try
        {
            InputStream data = stream.getData();

            byte[] buffer = new byte[1024];
            int length = 0;

            StreamDataMessage msg = new StreamDataMessage(stream.getMessage().getStreamID());
            msg.setReliable(true);

            while ((length = data.read(buffer)) != -1) {
                byte[] newBuffer = new byte[length];

                for (int i = 0; i != length; ++i) {
                    newBuffer[i] = buffer[i];
                }
                msg.setData(newBuffer);
                receiver.send(msg);
            }
            data.close();

            receiver.send(stream.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            log.log(Level.WARNING, "[StreamSender][TCP] Could not send stream with message {0} to {1}. Reason: {2}.", new Object[]{stream, receiver, ex.getMessage()});
        }
    }

    public void messageReceived(Message message) {
        if (message instanceof StreamMessage && !(message instanceof StreamDataMessage)) {
            // A stream was accepted.
            StreamMessage streamMessage = (StreamMessage)message;
            Stream stream = getStream(streamMessage);

            if (stream == null) return;
            stream.setAccepted(true);
            startStream(stream);
        }
    }

    private Stream getStream(short id) {
        for (Stream stream : streams) {
            if (stream.getMessage().getStreamID() == id) return stream;
        }
        return null;
    }

    private Stream getStream(StreamMessage msg) {
        for (Stream stream : streams) {
            if (stream.getMessage().getStreamID() == msg.getStreamID()) return stream;
        }
        return null;
    }
}
