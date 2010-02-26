package jme3test;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.system.G3DSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class DeleteMe extends AssetInfo {

    public static void main(String[] args){
        AssetManager am = G3DSystem.newAssetManager();
        am.registerLocator("E:\\NEWMODEL\\", "com.jme3.asset.plugins.FileSystemLocator", "*");
        File f  = new File("E:\\NEWMODEL\\terrainBridge_3.j3o");
        AssetKey key = new AssetKey(f.getName());
        BinaryImporter imp = new BinaryImporter();
        for (int i = 0; i < 10000; i++){
            imp.load(new DeleteMe(am, key, f));
        }
    }

    private File f;

    public DeleteMe(AssetManager am, AssetKey key, File f){
        super(am, key);
        this.f = f;
    }

    @Override
    public InputStream openStream() {
        try{
            return new FileInputStream(f);
        }catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        return null;
    }
}
