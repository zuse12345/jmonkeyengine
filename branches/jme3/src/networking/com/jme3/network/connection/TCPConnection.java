package com.jme3.network.connection;

import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

/**
 * The <code>TCPConnection</code> handles all traffic regarding TCP client and server.
 *
 * @author Lars Wesselius
 * @see Connection
 */
public class TCPConnection extends Connection {
    protected SocketChannel socketChannel;
    protected ServerSocketChannel serverSocketChannel;
    
    protected ByteBuffer    readBuffer;
    protected ByteBuffer    writeBuffer;
    protected ByteBuffer    tempWriteBuffer;

    protected final Object  writeLock = new Object();

    private int             objectLength = 0;

    public TCPConnection(String name)
    {
        label = name;

        readBuffer =        ByteBuffer.allocateDirect(16228);
        writeBuffer =       ByteBuffer.allocateDirect(16228);
        tempWriteBuffer =   ByteBuffer.allocateDirect(16228);
    }

    public TCPConnection() { }
    
    public void connect(SocketAddress address) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.socket().setTcpNoDelay(true);

        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        
        socketChannel.register(selector, SelectionKey.OP_CONNECT).attach(this);
        log.log(Level.INFO, "[{1}][TCP] Connecting to {0}", new Object[]{address, label});
    }

    public void bind(SocketAddress address) throws IOException {
        serverSocketChannel = selector.provider().openServerSocketChannel();
        serverSocketChannel.socket().bind(address);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        log.log(Level.INFO, "[{1}][TCP] Bound to {0}", new Object[]{address, label});
    }

    public void connect(SelectableChannel channel) throws IOException {
        ((SocketChannel)channel).finishConnect();
        socketChannel.keyFor(selector).interestOps(SelectionKey.OP_READ);
        fireClientConnected(null);
        log.log(Level.INFO, "[{0}][TCP] Connection succeeded.", label);
    }

    public void accept(SelectableChannel channel) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel)channel).accept();

        String reason = shouldFilterConnector((InetSocketAddress)socketChannel.socket().getRemoteSocketAddress());
        if (reason != null) {
            log.log(Level.INFO, "[{2}][TCP] Client with address {0} got filtered with reason: {1}", new Object[]{(InetSocketAddress)socketChannel.socket().getRemoteSocketAddress(), reason, label});
            socketChannel.close();
            return;
        }

        socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);

        Client con = new Client(true);
        con.setTCPConnection(this);
        con.setSocketChannel(socketChannel);

        socketChannel.register(selector, SelectionKey.OP_READ, con);

        connections.add(con);

        log.log(Level.INFO, "[{1}][TCP] A client connected with address {0}", new Object[]{socketChannel.socket().getInetAddress(), label});
    }

    public void read(SelectableChannel channel) throws IOException {
        SocketChannel socketChannel = (SocketChannel)channel;

        if (socketChannel == null) {
            log.log(Level.WARNING, "[{0}][TCP] Connection was closed before we could read.", label);
            return;
        }

        readBuffer.compact();
        int read = socketChannel.read(readBuffer);
        readBuffer.flip();
        if (read == -1) {
			socketChannel.keyFor(selector).cancel();
            if (serverSocketChannel != null) {
                log.log(Level.WARNING, "[{0}][TCP] Connection was closed before we could read. Disconnected client.", label);
			    addToDisconnectionQueue((Client)socketChannel.keyFor(selector).attachment());
            } else {
                log.log(Level.WARNING, "[{0}][TCP] Server closed connection. Disconnected.", label);
                fireClientDisconnected(null);
            }
			return;
        }

        log.log(Level.FINE, "[{1}][TCP] Read {0} bytes.", new Object[]{read, label});
        // Okay, see if we can read the data length.
        while (true) {
            try {

                // If we're currently not already reading an object, retrieve the length
                // of the next one.
                if (objectLength == 0) {
                    objectLength = readBuffer.getInt();
                }

                int pos = readBuffer.position();
                int oldLimit = readBuffer.limit();

                int dataLength = objectLength;
                if (dataLength > 0 && readBuffer.remaining() >= dataLength) {
                    // We can read a full object.
                    if (pos + dataLength + 4 > readBuffer.capacity()) {
                        readBuffer.limit(readBuffer.capacity());
                    } else {
                        readBuffer.limit(pos + dataLength + 4);
                    }
                    Object obj = Serializer.readClassAndObject(readBuffer);
                    readBuffer.limit(oldLimit);
                    objectLength = 0;
                    if (obj != null) {
                        if (obj instanceof Message) {
                            Message message = (Message)obj;

                            Object attachment = socketChannel.keyFor(selector).attachment();
                            if (attachment instanceof Client) message.setClient((Client)attachment);
                            message.setConnection(this);
                            this.fireMessageReceived(message);
                        } else {
                            this.fireObjectReceived(obj);
                        }
                        log.log(Level.FINEST, "[{0}][TCP] Read full object: {1}", new Object[]{label, obj});
                    }
                } else if (dataLength > readBuffer.remaining()) {
                    readBuffer.compact();
                    int bytesRead = socketChannel.read(readBuffer);
                    log.log(Level.FINEST, "[{0}][TCP] Object won't fit in buffer, so read {1} more bytes in a compacted buffer.", new Object[]{label, bytesRead});
                    readBuffer.flip();
                } else {
                    objectLength = dataLength;
                }
            } catch (BufferUnderflowException someEx) {
                log.log(Level.FINEST, "[{0}][TCP] Done reading messages.", new Object[]{label});
                break;
            }
        }

    }

    public void sendObject(Object object) throws IOException {
        if (serverSocketChannel == null) {
            send(socketChannel, object) ;
        } else {
            for (Client connector : connections) {
                send(connector.getSocketChannel(), object);
            }
        }
    }

    public void sendObject(Client con, Object object) throws IOException {
        if (object instanceof Message) ((Message)object).setClient(con);
        send(con.getSocketChannel(), object);
    }

    public void cleanup() throws IOException {
        if (serverSocketChannel != null) {
            serverSocketChannel.close();
            connections.clear();
        } else {
            socketChannel.close();
        }
    }

    protected void send(SocketChannel channel, Object object) throws IOException {
        synchronized (writeLock) {
            tempWriteBuffer.clear();
            tempWriteBuffer.position(4);
            Serializer.writeClassAndObject(tempWriteBuffer, object);
            tempWriteBuffer.flip();

            int dataLength = tempWriteBuffer.limit() - 4;
            tempWriteBuffer.position(0);
            tempWriteBuffer.putInt(dataLength);
            tempWriteBuffer.position(0);

            if (writeBuffer.position() > 0) {
                writeBuffer.put(tempWriteBuffer);
            } else {
                int writeLength = 0;
                while (tempWriteBuffer.hasRemaining()) {
                    int wrote = channel.write(tempWriteBuffer);
                    writeLength += wrote;
                    if (wrote == 0) {
                        break;
                    }
                }

                log.log(Level.FINE, "[{1}][TCP] Wrote {0} bytes.", new Object[]{writeLength, label});

                if (writeBuffer.hasRemaining()) {
                    writeBuffer.put(tempWriteBuffer);
                } else {
                    if (object instanceof Message) {
                        this.fireMessageSent((Message)object);
                    } else {
                        this.fireObjectSent(object);
                    }
                }
            }
        }
    }

    public void write(SelectableChannel channel) throws IOException {

        SocketChannel socketChannel = (SocketChannel)channel;

        synchronized (writeLock) {

            writeBuffer.flip();
            long bytes = writeBuffer.remaining();
            while (writeBuffer.hasRemaining()) {
                if (socketChannel.write(writeBuffer) == 0) break;
            }
            if (!writeBuffer.hasRemaining()) {
                socketChannel.keyFor(selector).interestOps(SelectionKey.OP_READ);
            }

            writeBuffer.compact();

            log.log(Level.FINE, "[{1}][TCP] Wrote {0} bytes.", new Object[]{bytes, label});
        }
    }
}
