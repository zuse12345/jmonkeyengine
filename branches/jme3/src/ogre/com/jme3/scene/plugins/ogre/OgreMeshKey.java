package com.jme3.scene.plugins.ogre;

import com.jme3.asset.AssetKey;
import com.jme3.asset.ModelKey;
import com.jme3.scene.Spatial;

public class OgreMeshKey extends ModelKey {

    private OgreMaterialList materialList;

    public OgreMeshKey(String name, OgreMaterialList materialList){
        super(name);
        this.materialList = materialList;
    }

    public OgreMaterialList getMaterialList() {
        return materialList;
    }
    
}
