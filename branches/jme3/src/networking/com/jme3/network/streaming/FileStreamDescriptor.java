package com.jme3.network.streaming;

/**
 * File stream descriptor implements methods for dealing with files sent
 *  across the network.
 *
 * @author Lars Wesselius
 */
public class FileStreamDescriptor extends StreamDescriptor {
    private String fileName;
    private String filePath;

    FileStreamDescriptor(FileMessage fMsg, byte[] data) {
        super(data);

        fileName = fMsg.getFileName();
        filePath = fMsg.getFilePath();
    }

    public String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String toString() {
        return "FileStreamDescriptor[fileName=" + fileName + ",filePath=" + filePath + ",data=" + data + "]";
    }
}
