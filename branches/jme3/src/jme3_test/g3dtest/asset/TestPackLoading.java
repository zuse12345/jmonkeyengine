package g3dtest.asset;

import com.g3d.asset.AssetManager;
import com.g3d.asset.pack.J3PFileLocator;
import com.g3d.texture.Texture;

public class TestPackLoading {
    public static void main(String[] args){
        AssetManager manager = new AssetManager(true);
        manager.registerLocator("town.j3p", J3PFileLocator.class, "*");
        Texture tex = manager.loadTexture("CasaRosa.jpg");
        System.out.println(tex);
    }
}
