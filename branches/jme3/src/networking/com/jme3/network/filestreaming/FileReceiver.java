package com.jme3.network.filestreaming;

import com.jme3.network.events.MessageListener;
import com.jme3.network.message.Message;
import com.jme3.network.message.StreamMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File receiver receives files sent by peers.
 *
 * @author Lars Wesselius
 */
public class FileReceiver implements MessageListener {

    protected ArrayList<FileListener>
                                fileListeners = new ArrayList<FileListener>();

    protected ArrayList<ReceiveSession>
                                sessions = new ArrayList<ReceiveSession>();

    public void addFileListener(FileListener listener) {
        fileListeners.add(listener);
    }

    public void removeFileListener(FileListener listener) {
        fileListeners.remove(listener);
    }

    private ReceiveSession getReceiveSession(short streamID) {
        for (ReceiveSession ses : sessions) {
            if (ses.streamID == streamID) return ses;
        }
        return null;
    }

    public void messageReceived(Message message) {
        if (message instanceof StreamMessage) {
            StreamMessage strMsg = (StreamMessage)message;

            ReceiveSession ses = getReceiveSession(strMsg.getID());

            if (ses == null) {
                // This is the start of a new session.
                ses = new ReceiveSession(strMsg.getID());
                sessions.add(ses);
            }

            ses.write(strMsg.getData());
        }
        if (message instanceof FileMessage) {
            FileMessage fMsg = (FileMessage)message;

            ReceiveSession ses = getReceiveSession(fMsg.getStreamID());
            if (ses == null) return;

            fMsg.setFile(ses.close());
            sessions.remove(ses);

            fireFileReceived(fMsg);
        }
    }

    public void messageSent(Message message) {
    }

    public void objectReceived(Object object) {
    }

    public void objectSent(Object object) {
    }

    private void fireFileReceived(FileMessage fMsg) {
        for (FileListener listener : fileListeners) {
            listener.fileReceived(fMsg);
        }
    }

    private class ReceiveSession {
        protected Logger log = Logger.getLogger(ReceiveSession.class.getName());

        public short streamID;

        private FileOutputStream outputStream;
        private File file;

        private boolean finished = false;

        public ReceiveSession(short id) {
            streamID = id;
            file = new File("SMTempFile" + id);
            try {
                outputStream = new FileOutputStream(file);
            } catch (Exception e) {
                log.log(Level.WARNING, "[FileReceiver][TCP] Could not open outputstream on file receive. Message: {0}.", new Object[]{e.getMessage()});
            }
        }

        public void write(byte[] data) {
            try {
                outputStream.write(data);
            } catch (IOException e) {
                log.log(Level.WARNING, "[FileReceiver][TCP] Could not write to outputstream. Message: {0}.", new Object[]{e.getMessage()});
            }
        }

        public File close() {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "[FileReceiver][TCP] Could not close outputstream. Message: {0}.", new Object[]{e.getMessage()});
            }
            finished = true;

            return file;
        }
    }
}
