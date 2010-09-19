package jme3test.network;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class TestHostDiscovery {
    public static void main(String[] args) throws IOException, InterruptedException{
        Server server = new Server(5110, 5110);
        server.start();

        Client client = new Client();
        client.start();

        List<InetAddress> hosts = client.discoverHosts(5110, 5000);
        for (InetAddress host : hosts){
            System.out.println("Found host: " + host);
            System.out.println("Reachable? " + host.isReachable(5000));
        }

        System.out.println("Connecting to: "+ hosts.get(0));
        client.connect(hosts.get(0).getCanonicalHostName(), 5110, 5110);
    }
}
