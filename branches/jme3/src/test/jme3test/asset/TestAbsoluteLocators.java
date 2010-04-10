package jme3test.asset;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.audio.AudioData;
import com.jme3.audio.plugins.WAVLoader;
import com.jme3.font.BitmapFont;
import com.jme3.font.plugins.BitmapFontLoader;
import com.jme3.texture.Texture;
import com.jme3.texture.plugins.AWTLoader;

public class TestAbsoluteLocators {
    public static void main(String[] args){
        AssetManager am = new DesktopAssetManager(false);

        am.registerLoader(AWTLoader.class.getName(), "png");
        am.registerLoader(WAVLoader.class.getName(), "wav");

        // register two absolute locators
        am.registerLocator("/sounds",  ClasspathLocator.class.getName(), "*");
        am.registerLocator("/textures",ClasspathLocator.class.getName(), "*");

        // find a sound
        AudioData audio = am.loadAudio("gun.wav");

        // find a texture
        Texture tex = am.loadTexture("pond1.PNG");


        if (audio == null)
            throw new RuntimeException("Cannot find audio!");

        if (tex == null)
            throw new RuntimeException("Cannot find texture!");

        System.out.println("Success!");
    }
}
