package jme3test.asset;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.system.G3DSystem;
import com.jme3.texture.Texture;

public class TestJarLoading {
    public static void main(String[] args){
        AssetManager manager = G3DSystem.newAssetManager();
        manager.registerLocator("town.zip", "com.jme3.asset.plugins.JARLocator", "*");
        Texture tex = manager.loadTexture("CasaRosa.jpg");
        System.out.println(tex);
    }
}
