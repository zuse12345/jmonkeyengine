package com.g3d.scene.plugins.ogre;

import com.g3d.asset.AssetKey;

public class OgreMeshKey extends AssetKey {

    private OgreMaterialList materialList;

    public OgreMeshKey(String name, OgreMaterialList materialList){
        super(name);
        this.materialList = materialList;
    }

    public OgreMaterialList getMaterialList() {
        return materialList;
    }
    
}
