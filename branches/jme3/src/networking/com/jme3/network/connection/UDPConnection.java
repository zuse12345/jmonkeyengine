package com.jme3.network.connection;

import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

/**
 * The <code>UDPConnection</code> handles all UDP traffic.
 *
 * @author Lars Wesselius
 */
public class UDPConnection extends Connection {
    protected DatagramChannel datagramChannel;

    protected ByteBuffer writeBuffer;
    protected ByteBuffer readBuffer;

    protected SocketAddress target = null;

    public UDPConnection(String label) {
        this.label = label;

        readBuffer =    ByteBuffer.allocateDirect(12228);
        writeBuffer =   ByteBuffer.allocateDirect(12228);
    }

    public void connect(SocketAddress address) throws IOException {
        datagramChannel = selector.provider().openDatagramChannel();
        datagramChannel.socket().bind(null);
        datagramChannel.socket().connect(address);
        datagramChannel.configureBlocking(false);

        datagramChannel.register(selector, SelectionKey.OP_READ);
        log.log(Level.INFO, "[{1}][UDP] Set target to {0}", new Object[]{address, label});
        target = address;
    }

    public void bind(SocketAddress address) throws IOException {
        datagramChannel = selector.provider().openDatagramChannel();
        datagramChannel.socket().bind(address);
        datagramChannel.configureBlocking(false);
        
        datagramChannel.register(selector, SelectionKey.OP_READ);

        log.log(Level.INFO, "[{1}][UDP] Bound to {0}", new Object[]{address, label});
    }

    public void connect(SelectableChannel channel) throws IOException {
        // UDP is connectionless.
    }

    public void accept(SelectableChannel channel) throws IOException {
        // UDP is connectionless.
    }

    public void read(SelectableChannel channel) throws IOException {
        DatagramChannel socketChannel = (DatagramChannel)channel;

        InetSocketAddress address = (InetSocketAddress)datagramChannel.receive(readBuffer);

        String reason = shouldFilterConnector(address);
        if (reason != null) {
            log.log(Level.INFO, "[Server][UDP] Client with address {0} got filtered with reason: {1}", new Object[]{(InetSocketAddress)socketChannel.socket().getRemoteSocketAddress(), reason});
            socketChannel.close();
            return;
        }

        SelectionKey key = socketChannel.keyFor(selector);
        if ((key.attachment() == null || ((Client)key.attachment()).getDatagramReceiver() != address) && target == null) {
            Client client = new Client(true);
            client.setDatagramReceiver(address);
            client.setUDPConnection(this);
            client.setDatagramChannel(socketChannel);
            connections.add(client);

            key.attach(client);
        }

        readBuffer.flip();

        Object object = Serializer.readClassAndObject(readBuffer);

        log.log(Level.FINE, "[{0}][UDP] Read full object: {1}", new Object[]{label, object});

        if (object instanceof Message) {
            Message message = (Message)object;

            Object attachment = socketChannel.keyFor(selector).attachment();
            if (attachment instanceof Client) message.setClient((Client)attachment);
            message.setConnection(this);
            this.fireMessageReceived(message);
        } else {
            this.fireObjectReceived(object);
        }

        readBuffer.clear();
    }

    protected void send(SocketAddress dest, Object object) {
        try {
            Serializer.writeClassAndObject(writeBuffer, object);
            writeBuffer.flip();

            int bytes = datagramChannel.send(writeBuffer, dest);

            if (object instanceof Message) {
                this.fireMessageSent((Message)object);
            } else {
                this.fireObjectSent(object);
            }

            log.log(Level.FINE, "[{0}][UDP] Wrote {1} bytes to {2}.", new Object[]{label, bytes, dest});
            writeBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObject(Object object) throws IOException {
        if (target == null) {
            // This is a UDP server.
            for (Client connector : connections) {
                send(connector.getDatagramReceiver(), object);
            }
        } else {
            send(target, object);
        }
    }

    public void sendObject(Client client, Object object) throws IOException {
        if (object instanceof Message) ((Message)object).setClient(client);
        send(client.getDatagramReceiver(), object);
    }

    public void cleanup() throws IOException {
        datagramChannel.close();

        if (target == null) {
            connections.clear();
        }
    }

    public void write(SelectableChannel channel) throws IOException {
        // UDP is (almost) always ready for data, so send() will do.
    }
}
