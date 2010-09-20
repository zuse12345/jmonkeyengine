package com.jme3.network.streaming;

import com.jme3.network.connection.Client;
import com.jme3.network.message.StreamMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Stream {
    private StreamMessage message;
    private InputStream data;
    private Client receiver;

    private ArrayList<StreamListener> listeners = new ArrayList<StreamListener>();

    private boolean accepted = false;

    public StreamMessage getMessage() {
        return message;
    }

    public void setMessage(StreamMessage message) {
        this.message = message;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public Client getReceiver() {
        return receiver;
    }

    public void setReceiver(Client receiver) {
        this.receiver = receiver;
    }

    public void addDataListener(StreamListener listener) {
        listeners.add(listener);
    }

    public void removeDataListener(StreamListener listener) {
        listeners.remove(listener);
    }

    public List<StreamListener> getDataListeners() {
        return listeners;
    }
}
