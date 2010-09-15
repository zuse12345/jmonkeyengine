package com.jme3.network.service;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class can extend Service Manager to support services.
 *
 * @author Lars Wesselius
 */
public class ServiceManager {
    private Logger log = Logger.getLogger(ServiceManager.class.getName());
    private final List<Service> services = new ArrayList<Service>();

    private boolean client;

    public static final boolean CLIENT = true;
    public static final boolean SERVER = false;

    public ServiceManager(boolean client) {
        this.client = client;
    }

    public <T> T getService(Class cls) {
        for (Service service : services) {
            if (service.getClass() == cls) return (T)service;
        }

        try {
            boolean fail = false;
            for (Class interf : cls.getInterfaces()) {
                if (!interf.getSimpleName().equals("Service")) fail = true;
            }

            if (fail) return null;


            Constructor ctor = null;

            if (client) {
                try {
                    ctor = cls.getConstructor(new Class[]{Client.class});
                } catch (NoSuchMethodException nsme) {
                    log.log(Level.WARNING, "[ServiceManager][???] The service {0} does not support client mode.", cls);
                    return null;
                }
            } else {
                try {
                    ctor = cls.getConstructor(new Class[]{Server.class});
                } catch (NoSuchMethodException nsme) {
                    log.log(Level.WARNING, "[ServiceManager][???] The service {0} does not support server mode.", cls);
                    return null;
                }
            }

            T inst = (T)ctor.newInstance(new Object[]{this});

            services.add((Service)inst);
            return inst;
        } catch (Exception e) {
            log.log(Level.SEVERE, "[ServiceManager][???] Instantiaton of service failed.", e);
        }
        return null;
    }
}
