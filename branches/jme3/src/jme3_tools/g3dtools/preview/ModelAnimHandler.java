package g3dtools.preview;

import com.g3d.animation.Model;
import com.g3d.app.Application;
import java.util.Collection;

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
        app.lock();
        Collection<String> anims = model.getAnimationNames();
        app.unlock();
        return anims;
    }

    public float getLength(String name) {
        app.lock();
        float len = model.getAnimationLength(name);
        app.unlock();
        return len;
    }

    public void play(String name) {
        app.lock();
        model.setAnimation(name);
        app.unlock();
    }

    public void blendTo(String name, float time) {
        app.lock();
        model.setAnimation(name);
        app.unlock();
    }

    public void setSpeed(float speed) {
    }

    public float getSpeed() {
        return 1.0f;
    }

    public String getCurrent() {
        app.lock();
        String cur = model.getCurrentAnimation();
        app.unlock();
        return cur;
    }

}
