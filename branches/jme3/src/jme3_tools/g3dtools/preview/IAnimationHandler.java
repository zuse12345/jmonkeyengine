package g3dtools.preview;

import com.g3d.app.Application;
import java.util.Collection;

public interface IAnimationHandler {

    public void setApp(Application app);

    public Collection<String> list();
    
    public float getLength(String name);
    
    public void play(String name);
    
    public void blendTo(String name, float time);
    
    public void setSpeed(float speed);
    
    public float getSpeed();
    
    public String getCurrent();
    
}
