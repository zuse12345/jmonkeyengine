package com.g3d.asset;

import com.g3d.font.BitmapFont;
import com.g3d.material.Material;
import com.g3d.scene.Spatial;
import com.g3d.shader.Shader;
import com.g3d.shader.ShaderKey;
import com.g3d.texture.Texture;

public interface AssetManager {
    public void registerLoader(String loaderClassName, String ... extensions);
    public void registerLocator(String rootPath, String locatorClassName, String ... extensions);
    public Object loadContent(AssetKey key);
    public Object loadContent(String name);
    public Texture loadTexture(TextureKey key);
    public Texture loadTexture(String name);
    public Spatial loadModel(String name);
    public Material loadMaterial(String name);
    public Shader loadShader(ShaderKey key);
    public BitmapFont loadFont(String name);
}
