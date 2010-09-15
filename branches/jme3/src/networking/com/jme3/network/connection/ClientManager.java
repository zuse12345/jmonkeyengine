package com.jme3.network.connection;

import com.jme3.network.events.ConnectionListener;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.ClientRegistrationMessage;
import com.jme3.network.message.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * The ClientManager is an internal class that deals with client registrations and disconnects.
 *
 * @author Lars Wesselius
 */
public class ClientManager extends MessageAdapter implements ConnectionListener {
    protected Logger log = Logger.getLogger(ClientManager.class.getName());
    private ArrayList<Client> clients = new ArrayList<Client>();
    private ArrayList<ClientRegistrationMessage> pendingMessages = new ArrayList<ClientRegistrationMessage>();

    private ArrayList<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();

    private ClientRegistrationMessage findMessage(long playerId) {
        for (ClientRegistrationMessage message : pendingMessages) {
            if (message.getId() == playerId) {
                return message;
            }
        }
        return null;
    }

    public List<Client> getConnectors() {
        return Collections.unmodifiableList(clients);
    }

    public Client getClient(long playerId) {
        for (Client client : clients) {
            if (client.getPlayerID() == playerId) return client;
        }
        return null;
    }


    @Override
    public void messageReceived(Message message) {
        ClientRegistrationMessage regMessage = (ClientRegistrationMessage)message;
        ClientRegistrationMessage existingMessage = findMessage(regMessage.getId());

        // Check if message exists, if not add this message to the pending queue.
        if (existingMessage == null) {
            pendingMessages.add(regMessage);
            return;
        }

        // We've got two messages of which we can construct a client.
        Client client = new Client(true);

        Connection conOne = regMessage.getConnection();
        Connection conTwo = existingMessage.getConnection();


        if (conOne instanceof TCPConnection) {
            fillInTCPInfo(client, regMessage);
        } else if (conOne instanceof UDPConnection) {
            fillInUDPInfo(client, regMessage);
        }

        if (conTwo instanceof TCPConnection) {
            fillInTCPInfo(client, existingMessage);
        } else if (conTwo instanceof UDPConnection) {
            fillInUDPInfo(client, existingMessage);
        }

        if (client.getUDPConnection() == null || client.getTCPConnection() == null) {
            // Something went wrong in this registration.
            log.severe("[ClientManager][???] Something went wrong in the client registration process.");
            return;
        }

        client.setPlayerID(regMessage.getId());

        // Set other clients to this playerID aswell.
        //regMessage.getClient().setPlayerID(regMessage.getId());
        //existingMessage.getClient().setPlayerID(regMessage.getId());


        fireClientConnected(client);

        // Remove pending message.
        pendingMessages.remove(existingMessage);
        clients.add(client);
    }

    private void fillInUDPInfo(Client client, ClientRegistrationMessage msg) {
        client.setUDPConnection((UDPConnection)msg.getConnection());
        client.setDatagramReceiver(msg.getClient().getDatagramReceiver());
        client.setDatagramChannel(msg.getClient().getDatagramChannel());

        client.getDatagramChannel().keyFor(msg.getConnection().selector).attach(client);
    }

    private void fillInTCPInfo(Client client, ClientRegistrationMessage msg) {
        client.setSocketChannel(msg.getClient().getSocketChannel());
        client.setTCPConnection((TCPConnection)msg.getConnection());

        client.getSocketChannel().keyFor(msg.getConnection().selector).attach(client);
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    public void clientConnected(Client client) {
    }

    public void clientDisconnected(Client client) {
        if (clients.contains(client)) {
            clients.remove(client);
            fireClientDisconnected(client);
        }
    }

    public void fireClientConnected(Client client) {
        for (ConnectionListener listener : connectionListeners) {
            listener.clientConnected(client);
        }
    }

    public void fireClientDisconnected(Client client) {
        for (ConnectionListener listener : connectionListeners) {
            listener.clientDisconnected(client);
        }
    }
}
