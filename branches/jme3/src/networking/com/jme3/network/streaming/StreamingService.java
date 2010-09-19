package com.jme3.network.streaming;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.Message;
import com.jme3.network.message.StreamDataMessage;
import com.jme3.network.message.StreamMessage;
import com.jme3.network.service.Service;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Streaming service handles all kinds of streaming to clients. It can be instantiated by
 *  both the client and server, where server will work as sender, and client as receiver.
 *
 * @author Lars Wesselius
 */
public class StreamingService extends MessageAdapter implements Service {
    private static Logger log = Logger.getLogger(StreamingService.class.getName());

    protected ArrayList<StreamListener>
                                fileListeners;

    protected ArrayList<ReceiveSession>
                                sessions;

    private Server server;
    private Client client;

    private short nextStreamID = Short.MIN_VALUE;

    public StreamingService(Server server) {
        this.server = server;
    }

    public StreamingService(Client client) {
        this.client = client;
        sessions = new ArrayList<ReceiveSession>();
        fileListeners = new ArrayList<StreamListener>();
        client.addMessageListener(this, StreamDataMessage.class,
                                        FileMessage.class,
                                        DataMessage.class);
    }

    // Server classes/methods //////////////////////////////////////////////////////////////

    /**
     * Send a file from a server to a client.
     *
     * @param receiver The given client.
     * @param file The file to send.
     */
    public void sendFile(Client receiver, File file) {
        try {
            FileMessage msg = new FileMessage();
            msg.setFileName(file.getName());
            msg.setFilePath(file.getCanonicalPath());
            sendStream(receiver, new FileInputStream(file), msg);
        } catch (Exception e) {
            log.log(Level.WARNING, "[StreamSender][TCP] Could not send file {0} to {1}. Reason: {2}.", new Object[]{file, receiver, e.getMessage()});
        }
    }

    /**
     * Send a stream from a server to a client.
     *
     * @param receiver The receiving client.
     * @param inStream The inputstream to send.
     */
    public void sendStream(Client receiver, InputStream inStream) {
        DataMessage msg = new DataMessage();
        sendStream(receiver, inStream, msg);
    }

    private void sendStream(Client receiver, InputStream inStream, StreamMessage message) {
        try {

            short streamID = ++nextStreamID;

            byte[] data = new byte[1024];
            int length = 0;

            StreamDataMessage msg = new StreamDataMessage(streamID);
            msg.setReliable(true);
            while ((length = inStream.read(data)) != -1) {
                byte[] newBuffer = new byte[length];

                for (int i = 0; i != length; ++i) {
                    newBuffer[i] = data[i];
                }
                msg.setData(newBuffer);
                receiver.send(msg);
            }
            inStream.close();

            message.setStreamID(streamID);
            message.setReliable(true);

            receiver.send(message);
        } catch (Exception e) {
            log.log(Level.WARNING, "[StreamSender][TCP] Could not send file/stream with message {0} to {1}. Reason: {2}.", new Object[]{message, receiver, e.getMessage()});
        }
    }

    /**
     * Send a byte array from a server to a client.
     *
     * @param receiver The receiving client.
     * @param data The byte array to send.
     */
    public void sendData(Client receiver, byte[] data) {
        DataMessage msg = new DataMessage();
        sendStream(receiver, new ByteArrayInputStream(data), msg);
    }

    /**
     * Helper method for sendData(Client, byte[], String).
     *
     * @param receiver The receiving client.
     * @param buffer The byte buffer to send.
     */
    public void sendData(Client receiver, ByteBuffer buffer) {
        DataMessage msg = new DataMessage();
        try {
            sendStream(receiver, new ByteArrayInputStream(buffer.array()), msg);
        } catch (UnsupportedOperationException uoe) {
            // Happens when the ByteBuffer is allocated directly.
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            sendStream(receiver, new ByteArrayInputStream(data), msg);
        }
    }

    // Client classes/methods //////////////////////////////////////////////////////////////

    public void addStreamListener(StreamListener listener) {
        fileListeners.add(listener);
    }

    public void removeFileListener(StreamListener listener) {
        fileListeners.remove(listener);
    }

    private ReceiveSession getReceiveSession(short streamID) {
        for (ReceiveSession ses : sessions) {
            if (ses.streamID == streamID) return ses;
        }
        return null;
    }

    public void messageReceived(Message message) {
        if (message instanceof StreamDataMessage) {
            StreamDataMessage strMsg = (StreamDataMessage)message;

            ReceiveSession ses = getReceiveSession(strMsg.getStreamID());

            if (ses == null) {
                // This is the start of a new session.
                ses = new ReceiveSession(strMsg.getStreamID());
                sessions.add(ses);
            }

            ses.write(strMsg.getData());
        }
        if (message instanceof FileMessage) {
            FileMessage fMsg = (FileMessage)message;

            ReceiveSession ses = getReceiveSession(fMsg.getStreamID());
            if (ses == null) return;

            sessions.remove(ses);


            FileStreamDescriptor fsd = new FileStreamDescriptor(fMsg, ses.close());
            fireStreamReceived(fsd);
        }
        if (message instanceof DataMessage) {
            DataMessage fMsg = (DataMessage)message;

            ReceiveSession ses = getReceiveSession(fMsg.getStreamID());
            if (ses == null) return;

            sessions.remove(ses);

            DataStreamDescriptor dsd = new DataStreamDescriptor(ses.close());
            fireStreamReceived(dsd);
        }
    }

    private void fireStreamReceived(StreamDescriptor descriptor) {
        for (StreamListener listener : fileListeners) {
            listener.streamReceived(descriptor);
        }
    }

    private class ReceiveSession {
        protected Logger log = Logger.getLogger(ReceiveSession.class.getName());

        public short streamID;

        private ByteArrayOutputStream outputStream;

        public ReceiveSession(short id) {
            streamID = id;
            try {
                outputStream = new ByteArrayOutputStream();
            } catch (Exception e) {
                log.log(Level.WARNING, "[StreamReceiver][TCP] Could not open outputstream on file receive. Message: {0}.", new Object[]{e.getMessage()});
            }
        }

        public void write(byte[] data) {
            try {
                outputStream.write(data);
            } catch (IOException e) {
                log.log(Level.WARNING, "[StreamReceiver][TCP] Could not write to outputstream. Message: {0}.", new Object[]{e.getMessage()});
            }
        }

        public byte[] close() {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "[StreamReceiver][TCP] Could not close outputstream. Message: {0}.", new Object[]{e.getMessage()});
            }
            return outputStream.toByteArray();
        }
    }
}
