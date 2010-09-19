package jme3test.network;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.MessageAdapter;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSerialization extends MessageAdapter {

    @Serializable
    public static class SomeObject {

        private int val;

        public SomeObject(){
        }

        public SomeObject(int val){
            this.val = val;
        }

        public int getVal(){
            return val;
        }

        public String toString(){
            return "SomeObject[val="+val+"]";
        }
    }

    public enum Status {
        High,
        Middle,
        Low;
    }

    @Serializable
    public static class TestSerializationMessage extends Message {

        boolean z;
        byte b;
        char c;
        short s;
        int i;
        float f;
        long l;
        double d;
        
        int[] ia;
        List<Object> ls;
        Map<String, SomeObject> mp;

        Status status1;
        Status status2;

        Date date;

        public TestSerializationMessage(){
            super(true);
        }

        public TestSerializationMessage(boolean initIt){
            super(true);
            if (initIt){
                z = true;
                b = -88;
                c = 'Y';
                s = 9999;
                i = 123;
                f = -75.4e8f;
                l = 9438345072805034L;
                d = -854834.914703e88;
                ia = new int[]{ 456, 678, 999 };

                ls = new ArrayList<Object>();
                ls.add("hello");
                ls.add(new SomeObject(-22));

                mp = new HashMap<String, SomeObject>();
                mp.put("abc", new SomeObject(555));

                status1 = Status.High;
                status2 = Status.Middle;

                date = new Date(System.currentTimeMillis());
            }
        }
    }

    @Override
    public void messageReceived(Message msg){
        TestSerializationMessage cm = (TestSerializationMessage) msg;
        System.out.println(cm.z);
        System.out.println(cm.b);
        System.out.println(cm.c);
        System.out.println(cm.s);
        System.out.println(cm.i);
        System.out.println(cm.f);
        System.out.println(cm.l);
        System.out.println(cm.d);
        System.out.println(Arrays.toString(cm.ia));
        System.out.println(cm.ls);
        System.out.println(cm.mp);
        System.out.println(cm.status1);
        System.out.println(cm.status2);
        System.out.println(cm.date);
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        Serializer.registerClass(SomeObject.class);
        Serializer.registerClass(TestSerializationMessage.class);

        Server server = new Server(5110, 5110);
        server.start();

        Client client = new Client("localhost", 5110, 5110);
        client.start();

        server.addMessageListener(new TestSerialization(), TestSerializationMessage.class);
        client.send(new TestSerializationMessage(true));
    }

}
