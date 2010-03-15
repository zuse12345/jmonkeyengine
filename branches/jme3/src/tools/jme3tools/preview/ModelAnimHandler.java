package jme3tools.preview;

import com.jme3.animation.Model;
import com.jme3.app.Application;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModelAnimHandler implements IAnimationHandler {

    private Model model;
    private Application app;

    public void setApp(Application app){
        this.app = app;
    }

    public ModelAnimHandler(Model model){
        this.model = model;
    }

    public Collection<String> list() {
        try{
            Collection<String> anims = (Collection<String>) app.enqueue(new Callable<Collection<String>>(){
            public Collection<String> call() throws Exception {
                return model.getAnimationNames();
            }
        }).get();
            return anims;
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public float getLength(final String name) {
        try{
            float len = app.enqueue(new Callable<Float>() {
                public Float call() throws Exception {
                    return model.getAnimationLength(name);
                }
            }).get();
            return len;
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public void play(final String name) {
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                model.setAnimation(name);
                return null;
            }
        });
    }

    public void blendTo(String name, float time) {
        play(name);
    }

    public void setSpeed(final float speed) {
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                model.setSpeed(speed);
                return null;
            }
        });
    }

    public float getSpeed() {
        return 1.0f;
    }

    public String getCurrent() {
        try{
            return app.enqueue(new Callable<String>() {
                public String call() throws Exception {
                    return model.getCurrentAnimation();
                }
            }).get();
        }catch (InterruptedException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ExecutionException ex){
            Logger.getLogger(ModelAnimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
