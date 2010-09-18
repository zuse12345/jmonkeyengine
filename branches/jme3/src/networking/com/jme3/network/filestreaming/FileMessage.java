package com.jme3.network.filestreaming;

import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;
import java.io.File;

/**
 * The File message is sent when a file send has been completed.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class FileMessage extends Message {
    private short streamID;
    private String fileName;
    private String filePath;
    private transient File file;

    public short getStreamID() {
        return streamID;
    }

    public void setStreamID(short streamID) {
        this.streamID = streamID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
