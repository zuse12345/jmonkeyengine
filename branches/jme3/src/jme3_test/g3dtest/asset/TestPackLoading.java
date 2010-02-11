package g3dtest.asset;

import com.g3d.asset.AssetManager;
import com.g3d.asset.TextureKey;
import com.g3d.system.G3DSystem;
import com.g3d.texture.Texture;

public class TestPackLoading {
    public static void main(String[] args){
        AssetManager manager = G3DSystem.newAssetManager();
        manager.registerLocator("town.j3p", "com.g3d.asset.pack.J3PFileLocator", "*");
        Texture tex = manager.loadTexture("CasaRosa.jpg");
        System.out.println(tex);
    }
}
