package com.g3d.ui;

import com.g3d.bounding.BoundingBox;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.res.ContentManager;
import com.g3d.scene.Geometry;
import com.g3d.scene.Quad;
import com.g3d.texture.Texture2D;

/**
 * A <code>Picture</code> represents a 2D image drawn on the screen.
 * It can be used to represent sprites or other background elements.
 *
 * @author Momoko_Fan
 */
public class Picture extends Geometry {

    private float width;
    private float height;

    public Picture(String name){
        super(name, new Quad(1, 1, false));
        setCullHint(CullHint.Never);
    }

    public void setWidth(float width){
        this.width = width;
        setLocalScale(new Vector3f(width, height, 1f));
    }

    public void setHeight(float height){
        this.height = height;
        setLocalScale(new Vector3f(width, height, 1f));
    }

    public void setPosition(float x, float y){
        setLocalTranslation(x, y, 0);
    }

    public void setImage(ContentManager manager, String imgName, boolean useAlpha){
        if (getMaterial() == null){
            Material mat = new Material(manager, "sprite2d.j3md");
            setMaterial(mat);
        }
        material.setTexture("m_Texture", manager.loadTexture(imgName));
    }

    public void setTexture(ContentManager manager, Texture2D tex, boolean useAlpha){
        if (getMaterial() == null){
            Material mat = new Material(manager, "sprite2d.j3md");
            setMaterial(mat);
        }
        material.setTexture("m_Texture", tex);
    }

}
