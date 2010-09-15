package com.jme3.network.connection;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The connection runnable takes the UDP and TCP connections
 *  and updates them accordingly.
 *
 * @author Lars Wesselius
 */
public class ConnectionRunnable implements Runnable {
    protected Logger log = Logger.getLogger(Server.class.getName());

    private TCPConnection tcp;
    private UDPConnection udp;
    private int delay = 30;
    private boolean keepAlive = true;
    private boolean alive = true;

    public ConnectionRunnable(TCPConnection tcp, UDPConnection udp, int delay)
    {
        this.tcp = tcp;
        this.udp = udp;
        this.delay = delay;
    }

    public ConnectionRunnable(TCPConnection tcp, UDPConnection udp)
    {
        this.tcp = tcp;
        this.udp = udp;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isRunning() {
        return alive;
    }

    public void run() {
        while (keepAlive)
        {
            // Run while one of the connections is still live.
            tcp.run();
            udp.run();

            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else { Thread.yield(); }
        }
        try
        {
            tcp.cleanup();
            udp.cleanup();
        } catch (IOException e) {
            log.log(Level.WARNING, "[???][???] Could not clean up the connection.", e);
            return;
        }
        alive = false;
        log.log(Level.FINE, "[???][???] Cleaned up TCP/UDP.");
    }
}
