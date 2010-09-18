package com.jme3.network.filestreaming;

import com.jme3.network.connection.Client;
import com.jme3.network.message.StreamMessage;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File sender sends files to a peer.
 *
 * @author Lars Wesselius
 */
public class FileSender {
    private static Logger log = Logger.getLogger(FileSender.class.getName());
    private static short nextStreamID = Short.MIN_VALUE;

    public static void sendFile(Client receiver, File file) {
        try {

            FileInputStream fis = new FileInputStream(file);

            short streamID = ++nextStreamID;

            byte[] data = new byte[1024];
            int length = 0;

            StreamMessage msg = new StreamMessage(streamID);
            msg.setReliable(true);
            while ((length = fis.read(data)) != -1) {
                byte[] newBuffer = new byte[length];

                for (int i = 0; i != length; ++i) {
                    newBuffer[i] = data[i];
                }
                msg.setData(newBuffer);
                receiver.send(msg);
            }
            fis.close();

            FileMessage fMessage = new FileMessage();
            fMessage.setReliable(true);
            fMessage.setStreamID(streamID);
            fMessage.setFileName(file.getName());
            fMessage.setFilePath(file.getCanonicalPath());

            receiver.send(fMessage);
        } catch (Exception e) {
            log.log(Level.WARNING, "[FileSender][TCP] Could not send file {0} to {1}. Reason: {2}.", new Object[]{file, receiver, e.getMessage()});
        }
    }
}
