package jme3test.network;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;

public class TestMessages {

    @Serializable
    public static class PingMessage extends Message {
    }

    @Serializable
    public static class PongMessage extends Message {
    }

    private static class PingResponder extends MessageAdapter {
        @Override
        public void messageReceived(Message message) {
            try {
                if (message instanceof PingMessage){
                    System.out.println("Received ping message!");
                    System.out.println("Sending pong message..");
                    message.getClient().send(new PongMessage());
                }else if (message instanceof PongMessage){
                    System.out.println("Received pong message!");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        Serializer.registerClass(PingMessage.class);
        Serializer.registerClass(PongMessage.class);

        Server server = new Server(5110, 5110);
        server.start();

        Client client = new Client("localhost", 5110, 5110);
        client.start();

        server.addMessageListener(new PingResponder(), PingMessage.class);
        client.addMessageListener(new PingResponder(), PongMessage.class);

        System.out.println("Sending ping message..");
        client.send(new PingMessage());
    }
}
