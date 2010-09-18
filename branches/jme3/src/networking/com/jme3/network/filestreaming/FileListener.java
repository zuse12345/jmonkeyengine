package com.jme3.network.filestreaming;

/**
 * Used for file sending/receiving.
 *
 * @author Lars Wesselius
 */
public interface FileListener {
    public void fileReceived(FileMessage msg);
}
