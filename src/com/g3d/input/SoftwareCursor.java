package com.g3d.input;

import com.g3d.input.binding.BindingListener;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Quad;
import com.g3d.texture.Image;
import com.g3d.texture.Texture.MagFilter;
import com.g3d.texture.Texture.MinFilter;
import com.g3d.texture.Texture2D;

@Deprecated
public class SoftwareCursor extends Geometry implements BindingListener {

    private Quad quad;
    private Texture2D tex = new Texture2D();
//    private TextureMaterial texMat = new TextureMaterial(false);

    private int screenWidth, screenHeight;
    private int cursorWidth, cursorHeight;

    public SoftwareCursor(){
        super("Software Cursor", new Quad(1,1));
        quad = (Quad) getMesh();
        quad.updateGeometry(1, 1, true);
        
        // setup texture
        tex.setAnisotropicFilter(0);
        tex.setMagFilter(MagFilter.Bilinear);
        tex.setMinFilter(MinFilter.BilinearNoMipMaps);

        // setup material
//        texMat.setTexture(tex);
//        setMaterial(texMat);
    }

    public void setPosition(int x, int y){
        setLocalTranslation(x, y, 0);
    }

    public void setImage(Image img, int screenWidth, int screenHeight){
        tex.setImage(img);
        cursorWidth = img.getWidth() * 3;
        cursorHeight = img.getHeight() * 3;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        setLocalScale(new Vector3f(cursorWidth / (float)screenWidth, cursorHeight / (float)screenHeight, 1f));
    }

    public void registerWithDispatcher(Dispatcher dispacher){
        dispacher.setCursorVisible(false);

        dispacher.registerMouseAxisBinding("MOUSE_Left", 0, true);
        dispacher.registerMouseAxisBinding("MOUSE_Right", 0, false);
        dispacher.registerMouseAxisBinding("MOUSE_Up", 1, false);
        dispacher.registerMouseAxisBinding("MOUSE_Down", 1, true);

        dispacher.addTriggerListener(this);
    }

    public void onBinding(String binding, float value) {
        Vector3f translate = new Vector3f();
        if (binding.equals("MOUSE_Left")){
            translate.setX(-value);
        }else if (binding.equals("MOUSE_Right")){
            translate.setX(value);
        }else if (binding.equals("MOUSE_Up")){
            translate.setY(value);
        }else if (binding.equals("MOUSE_Down")){
            translate.setY(-value);
        }
        translate.multLocal(new Vector3f(1000f / screenWidth, 1000f / screenHeight, 0f));
        Vector3f translation = getLocalTranslation();
        setLocalTranslation(translation.addLocal(translate));
    }

}
