package com.jme3.asset;

import com.jme3.audio.AudioData;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.texture.Texture;

public interface AssetManager {
    public void registerLoader(String loaderClassName, String ... extensions);
    public void registerLocator(String rootPath, String locatorClassName, String ... extensions);
    public Object loadAsset(AssetKey key);
    public Object loadAsset(String name);
    public Texture loadTexture(TextureKey key);
    public Texture loadTexture(String name);
    public AudioData loadAudio(AudioKey key);
    public AudioData loadAudio(String name);
    public Spatial loadModel(String name);
    public Material loadMaterial(String name);
    public Shader loadShader(ShaderKey key);
    public BitmapFont loadFont(String name);
}
