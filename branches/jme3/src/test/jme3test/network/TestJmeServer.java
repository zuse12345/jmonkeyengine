package jme3test.network;

import com.jme3.app.SimpleApplication;
import com.jme3.export.Savable;
import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.rmi.ObjectStore;
import com.jme3.network.serializing.Serializer;
import com.jme3.network.serializing.serializers.SavableSerializer;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.util.concurrent.Callable;

public class TestJmeServer {

    private static SimpleApplication serverApp;

    public static interface ServerAccess {
        public void attachChild(String model);
    }

    public static class ServerAccessImpl {
        public void attachChild(String model) {
            final String finalModel = model;
            serverApp.enqueue(new Callable<Void>() {
                public Void call() throws Exception {
                    Spatial spatial = serverApp.getAssetManager().loadModel(finalModel);
                    serverApp.getRootNode().attachChild(spatial);
                    return null;
                }
            });
        }
    }

    public static void createServer(){
        serverApp = new SimpleApplication() {
            @Override
            public void simpleInitApp() {
            }
        };
        serverApp.start();

        try {
            Server server = new Server(5110, 5110);
            server.start();

            ObjectStore store = new ObjectStore(server);
            store.exposeObject("access", new ServerAccessImpl());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        Serializer.registerClass(Savable.class, new SavableSerializer());

        createServer();

        Client client = new Client("localhost", 5110, 5110);
        client.start();

        ObjectStore store = new ObjectStore(client);
        ServerAccess access = store.getExposedObject("access", ServerAccess.class, true);
        access.attachChild("Models/Ferrari/WheelBackLeft.mesh.xml");
    }
}
