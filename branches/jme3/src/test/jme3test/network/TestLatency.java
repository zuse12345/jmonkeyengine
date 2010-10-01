package jme3test.network;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import com.jme3.network.sync.MovingAverage;
import java.io.IOException;

public class TestLatency extends MessageAdapter {

    private static long startTime;
    private static Client client;
    private static MovingAverage average = new MovingAverage(100);

    static {
        startTime = System.currentTimeMillis();
    }

    private static long getTime(){
        return System.currentTimeMillis() - startTime;
    }

    @Serializable
    public static class TimestampMessage extends Message {

        long timeSent     = 0;
        long timeReceived = 0;

        public TimestampMessage(){
            setReliable(false);
        }

        public TimestampMessage(long timeSent, long timeReceived){
            setReliable(false);
            this.timeSent = timeSent;
            this.timeReceived = timeReceived;
        }

    }

    @Override
    public void messageReceived(Message msg){
        TimestampMessage timeMsg = (TimestampMessage) msg;
        try {
            if (timeMsg.timeReceived == 0){
                TimestampMessage outMsg = new TimestampMessage(timeMsg.timeSent, getTime());
                msg.getClient().send(outMsg);
            }else{
                long curTime = getTime();
                //System.out.println("Time sent: " + timeMsg.timeSent);
                //System.out.println("Time received by server: " + timeMsg.timeReceived);
                //System.out.println("Time recieved by client: " + curTime);

                long latency = (curTime - timeMsg.timeSent);
                System.out.println("Latency: " + (latency) + " ms");
                //long timeOffset = ((timeMsg.timeSent + curTime) / 2) - timeMsg.timeReceived;
                //System.out.println("Approximate timeoffset: "+ (timeOffset) + " ms");

                average.add(latency);
                System.out.println("Average latency: " + average.getAverage());

                long latencyOffset = latency - average.getAverage();
                System.out.println("Latency offset: " + latencyOffset);

                client.send(new TimestampMessage(getTime(), 0));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        Serializer.registerClass(TimestampMessage.class);

        Server server = new Server(5110, 5110);
        server.start();

        client = new Client("localhost", 5110, 5110);
        client.start();

        client.addMessageListener(new TestLatency(), TimestampMessage.class);
        server.addMessageListener(new TestLatency(), TimestampMessage.class);

        Thread.sleep(1);

        client.send(new TimestampMessage(getTime(), 0));
    }

}
