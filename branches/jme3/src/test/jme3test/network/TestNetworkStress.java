package jme3test.network;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.ConnectionAdapter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestNetworkStress extends ConnectionAdapter {
    
    @Override
    public void clientConnected(Client client) {
        System.out.println("CLIENT CONNECTED: "+client.getClientID());
        try {
            client.kick("goodbye");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException{
        Logger.getLogger("").getHandlers()[0].setLevel(Level.OFF);
        
        Server server = new Server(5110, 5110);
        server.start();
        server.addConnectionListener(new TestNetworkStress());

        for (int i = 0; i < 1000; i++){
            Client client = new Client("localhost", 5110, 5110);
            client.start();

            Thread.sleep(10);
        }
    }
}
