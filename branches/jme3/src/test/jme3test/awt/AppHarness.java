/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jme3test.awt;

import com.jme3.app.Application;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeSystem;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.SwingUtilities;

/**
 *
 * @author Kirill
 */
public class AppHarness extends Applet {

    private JmeCanvasContext context;
    private Canvas canvas;
    private Application app;

    private String appClass;
    private URL appCfg = null;

    private void createCanvas(){
        AppSettings settings = new AppSettings(true);

        // load app cfg
        if (appCfg != null){
            try {
                InputStream in = appCfg.openStream();
                settings.load(in);
                in.close();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }

        settings.setWidth(getWidth());
        settings.setHeight(getHeight());
        settings.setAudioRenderer(null);

        JmeSystem.setLowPermissions(true);

        try{
            Class<? extends Application> clazz = (Class<? extends Application>) Class.forName(appClass);
            app = clazz.newInstance();
        }catch (ClassNotFoundException ex){
            ex.printStackTrace();
        }catch (InstantiationException ex){
            ex.printStackTrace();
        }catch (IllegalAccessException ex){
            ex.printStackTrace();
        }

        app.setSettings(settings);
        app.createCanvas();
        
        context = (JmeCanvasContext) app.getContext();
        canvas = context.getCanvas();
        canvas.setSize(getWidth(), getHeight());
        
        add(canvas);
        app.startCanvas();
    }

    @Override
    public final void update(Graphics g) {
        canvas.setSize(getWidth(), getHeight());
    }

    @Override
    public void init(){
        appClass = getParameter("AppClass");
        if (appClass == null)
            throw new RuntimeException("The required parameter AppClass isn't specified!");
        
        try {
            appCfg = new URL(getParameter("AppSettingsURL"));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            appCfg = null;
        }
        
        createCanvas();
        System.out.println("applet:init");
    }

    @Override
    public void start(){
        context.setAutoFlushFrames(true);
        System.out.println("applet:start");
    }

    @Override
    public void stop(){
        context.setAutoFlushFrames(false);
        System.out.println("applet:stop");
    }

    @Override
    public void destroy(){
        System.out.println("applet:destroyStart");
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                removeAll();
                System.out.println("applet:destroyRemoved");
            }
        });
        app.stop(true);
        System.out.println("applet:destroyDone");
    }

}
