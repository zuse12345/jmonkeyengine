package com.jme3.network.connection;

import com.jme3.network.events.ConnectionListener;
import com.jme3.network.events.MessageListener;
import com.jme3.network.message.ClientRegistrationMessage;
import com.jme3.network.message.DisconnectMessage;
import com.jme3.network.message.Message;
import com.jme3.network.service.ServiceManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends ServiceManager implements MessageListener, ConnectionListener {
    protected Logger            log = Logger.getLogger(Client.class.getName());

    protected static int        clientIDCounter = 0;
    protected int               clientID;
    protected long              playerID = -1;
    protected String            label;

    protected boolean           isConnected;
    protected TCPConnection     tcp;
    protected UDPConnection     udp;

    protected ConnectionRunnable
                                thread;

    // Client (connector) related.
    protected SocketChannel     tcpChannel;
    protected DatagramChannel   udpChannel;

    protected SocketAddress     udpTarget;

    protected boolean           isConnector;

    /**
     * Constructs this client.
     */
    public Client() {
        this(false);
    }

    /**
     * Construct this client, either as a server connector, or
     *  a real client. Internal method.
     *
     * @param connector Whether this client is a connector or not.
     */
    Client(boolean connector) {
        super(ServiceManager.CLIENT);
        clientID = ++clientIDCounter;
        this.label = "Client#" + clientID;

        isConnector = connector;
        if (connector) isConnected = true;

        if (tcp == null) tcp = new TCPConnection(label);
        if (udp == null) udp = new UDPConnection(label);
    }

    /**
     * Constructor providing custom instances of the clients and its addresses.
     *
     * @param tcp The TCPConnection instance to manage.
     * @param udp The UDPConnection instance to manage.
     * @param tcpAddress The TCP address to connect to.
     * @param udpAddress The UDP address to connect to.
     * @throws java.io.IOException When a connect error has occured.
     */
    public Client(TCPConnection tcp, UDPConnection udp, SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException {
        this();

        this.tcp = tcp;
        tcp.connect(tcpAddress);

        this.udp = udp;
        udp.connect(udpAddress);
        isConnected = true;

        tcp.addIndividualMessageListener(DisconnectMessage.class, this);
        udp.addIndividualMessageListener(DisconnectMessage.class, this);
        tcp.addConnectionListener(this);
    }

    /**
     * Constructor for providing a TCP client instance. UDP will be disabled.
     *
     * @param tcp The TCPConnection instance.
     * @param tcpAddress The address to connect to.
     * @throws IOException When a connection error occurs.
     */
    public Client(TCPConnection tcp, SocketAddress tcpAddress) throws IOException {
        this();

        this.tcp = tcp;
        tcp.connect(tcpAddress);
        isConnected = true;

        tcp.addIndividualMessageListener(DisconnectMessage.class, this);
        tcp.addConnectionListener(this);
    }

    /**
     * Constructor for providing a UDP client instance. TCP will be disabled.
     *
     * @param udp The UDP client instance.
     * @param updAddress The address to connect to.
     * @throws IOException When a connection error occurs.
     */
    public Client(UDPConnection udp, SocketAddress updAddress) throws IOException {
        this();

        this.udp = udp;
        udp.connect(updAddress);
        isConnected = true;

        udp.addIndividualMessageListener(DisconnectMessage.class, this);
    }

    /**
     * Simple constructor for providing TCP port and UDP port. Will bind using on
     *  all interfaces, on given ports.
     *
     * @param ip The IP address where the server are located.
     * @param tcpPort The TCP port to use.
     * @param udpPort The UDP port to use.
     * @throws IOException When a connection error occurs.
     */
    public Client(String ip, int tcpPort, int udpPort) throws IOException {
        this();

        tcp = new TCPConnection(label);
        tcp.connect(new InetSocketAddress(ip, tcpPort));

        udp = new UDPConnection(label);
        udp.connect(new InetSocketAddress(ip, udpPort));
        isConnected = true;

        tcp.addIndividualMessageListener(DisconnectMessage.class, this);
        udp.addIndividualMessageListener(DisconnectMessage.class, this);
        tcp.addConnectionListener(this);
    }

    /**
     * Connect method for when the no arg constructor was used.
     *
     * @param ip The IP address to connect to.
     * @param tcpPort The TCP port to use. To turn off, use -1.
     * @param udpPort The UDP port to use. To turn off, use -1.
     * @throws IllegalArgumentException When an illegal argument was given.
     * @throws java.io.IOException When a connection error occurs.
     */
    public void connect(String ip, int tcpPort, int udpPort) throws IllegalArgumentException, IOException {
        if (tcpPort == -1 && udpPort == -1) throw new IllegalArgumentException("No point in connect when you want to turn both the connections off.");

        if (tcpPort != -1) {
            tcp.connect(new InetSocketAddress(ip, tcpPort));
            tcp.addIndividualMessageListener(DisconnectMessage.class, this);
            tcp.addConnectionListener(this);
        }
        if (udpPort != -1) {
            udp.connect(new InetSocketAddress(ip, udpPort));
            udp.addIndividualMessageListener(DisconnectMessage.class, this);
        }
        isConnected = true;
    }

    /**
     * Send a message over TCP. If this client is a connector, it'll simply send to this
     *  connector. If the client is a normal client, this'll be sent to the server.
     *
     * @param object The object to send.
     * @throws IOException When a writing error occurs.
     */
    public void sendTCP(Object object) throws IOException {
        if (tcp == null) throw new IOException("No TCP client/server.");
        if (!isConnected) throw new IOException("Not connected yet. Use connect() first.");
        if (isConnector) {
            tcp.sendObject(this, object);
        } else {
            tcp.sendObject(object);
        }
    }

    /**
     * Send a message over UDP. If this client is a connector, it'll simply send to this
     *  connector. If the client is a normal client, this'll be sent to the server.
     *
     * @param object The object to send.
     * @throws IOException When a writing error occurs.
     */
    public void sendUDP(Object object) throws IOException {
        if (udp == null) throw new IOException("No UDP client/server.");
        if (!isConnected) throw new IOException("Not connected yet. Use connect() first.");
        if (isConnector) {
            udp.sendObject(this, object);
        } else {
            udp.sendObject(object);
        }
    }

    /**
     * Disconnect from the server.
     *
     * @param type See DisconnectMessage for the available types.
     * @throws IOException When a disconnection error occurs.
     */
    public void disconnect(String type) throws IOException {
        if (isConnector) return;
        // Send a disconnect message to the server.
        DisconnectMessage msg = new DisconnectMessage();
        msg.setType(type);
        tcp.sendObject(msg);
        udp.sendObject(msg);

        // We can disconnect now.
        thread.setKeepAlive(false);

        // GC it.
        thread = null;

        log.log(Level.INFO, "[{0}][???] Disconnected.", label);
    }

    /**
     * Disconnect from the server with the default disconnection type:
     *  USER_REQUESTED.
     *
     * @throws IOException When a disconnection error occurs.
     */
    public void disconnect() throws IOException {
        disconnect(DisconnectMessage.USER_REQUESTED);
    }

    /**
     * Kick this client from the server, with given kick reason.
     *
     * @param reason The reason this client was kicked.
     * @throws IOException When a writing error occurs.
     */
    public void kick(String reason) throws IOException {
        if (!isConnector) return;
        DisconnectMessage message = new DisconnectMessage();
        message.setType(DisconnectMessage.KICK);
        message.setReason(reason);
        sendTCP(message);

        tcp.addToDisconnectionQueue(this);

        log.log(Level.INFO, "[Server#?][???] {0} got kicked with reason: {1}.", new Object[]{this, reason});
    }

    private void disconnect(Message message) throws IOException {
        DisconnectMessage dcMessage = (DisconnectMessage)message;
        String type = dcMessage.getType();
        String reason = dcMessage.getReason();

        log.log(Level.INFO, "[{0}][???] We got disconnected from the server ({1}: {2}).", new Object[]{
                label,
                type,
                reason
        });

        // We can disconnect now.
        thread.setKeepAlive(false);

        // GC it.
        thread = null;

        isConnected = false;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    ///////////////

    // Server client related stuff.

    public void setSocketChannel(SocketChannel channel) {
        tcpChannel = channel;
    }

    public SocketChannel getSocketChannel() {
        return tcpChannel;
    }

    public void setDatagramChannel(DatagramChannel channel) {
        udpChannel = channel;
    }

    public DatagramChannel getDatagramChannel() {
        return udpChannel;
    }

    public void setDatagramReceiver(SocketAddress address) {
        udpTarget = address;
    }

    public SocketAddress getDatagramReceiver() {
        return udpTarget;
    }

    public void setTCPConnection(TCPConnection con) {
        tcp = con;
    }

    public void setUDPConnection(UDPConnection con) {
        udp = con;
    }

    public TCPConnection getTCPConnection() {
        return tcp;
    }

    public UDPConnection getUDPConnection() {
        return udp;
    }

    ///////////////

    /**
     * Start this client.
     */
    public void start()
    {
        new Thread(thread = new ConnectionRunnable(tcp, udp)).start();
    }
    
    public int getClientID() {
        return clientID;
    }

    public long getPlayerID() {
        return playerID;
    }

    public void setPlayerID(long id) {
        playerID = id;
    }

    public String toString() {
        return label;
    }

    public void addConnectionListener(ConnectionListener listener) {
        if (tcp != null) tcp.addConnectionListener(listener);
        if (udp != null) udp.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        if (tcp != null) tcp.removeConnectionListener(listener);
        if (udp != null) udp.removeConnectionListener(listener);
    }

    public void addMessageListener(MessageListener listener) {
        if (tcp != null) tcp.addMessageListener(listener);
        if (udp != null) udp.addMessageListener(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        if (tcp != null) tcp.removeMessageListener(listener);
        if (udp != null) udp.removeMessageListener(listener);
    }

    public void addIndividualMessageListener(Class messageClass, MessageListener listener) {
        if (tcp != null) tcp.addIndividualMessageListener(messageClass, listener);
        if (udp != null) udp.addIndividualMessageListener(messageClass, listener);
    }

    public void addIndividualMessageListener(Class[] messageClass, MessageListener listener) {
        for (Class c : messageClass) {
            if (tcp != null) tcp.addIndividualMessageListener(c, listener);
            if (udp != null) udp.addIndividualMessageListener(c, listener);
        }
    }

    public void removeIndividualMessageListener(Class messageClass, MessageListener listener) {
        if (tcp != null) tcp.removeIndividualMessageListener(messageClass, listener);
        if (udp != null) udp.removeIndividualMessageListener(messageClass, listener);
    }

    public void removeIndividualMessageListener(Class[] messageClass, MessageListener listener) {
        for (Class c : messageClass) {
            if (tcp != null) tcp.removeIndividualMessageListener(c, listener);
            if (udp != null) udp.removeIndividualMessageListener(c, listener);
        }
    }

    public void messageReceived(Message message) {
        try {
            disconnect(message);
        } catch (IOException e) {
            log.log(Level.WARNING, "[{0}][???] Could not disconnect.", label);
        }
    }

    public void messageSent(Message message) {

    }

    public void objectReceived(Object object) {

    }

    public void objectSent(Object object) {

    }

    public void clientConnected(Client client) {
        // We are a client. This means that we succeeded in connecting to the server.
        if (!isConnected) return;
        long time = System.currentTimeMillis();
        playerID = time;
        ClientRegistrationMessage message = new ClientRegistrationMessage();
        message.setId(time);
        try {
            sendTCP(message);
            sendUDP(message);
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "[{0}][???] Could not sent client registration message. Disconnecting.", label);
            try {
                disconnect(DisconnectMessage.ERROR);
            } catch (IOException ie) {}
        }
    }

    public void clientDisconnected(Client client) {
        if (thread != null) {
            // We can disconnect now.
            thread.setKeepAlive(false);

            // GC it.
            thread = null;
        }
    }
}
