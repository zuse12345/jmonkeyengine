/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.asset;

import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioData;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderKey;
import com.jme3.texture.Texture;

/**
 * <code>AssetManager</code> provides an interface for managing the data assets
 * of a jME3 application.
 */
public interface AssetManager {

    /**
     * Registers a loader for the given extensions.
     * @param loaderClassName
     * @param extensions
     */
    public void registerLoader(String loaderClassName, String ... extensions);

    /**
     *
     * @param rootPath
     * @param locatorClassName
     */
    public void registerLocator(String rootPath, String locatorClassName);

    /**
     *
     * @param loaderClass
     * @param extensions
     */
    public void registerLoader(Class<? extends AssetLoader> loaderClass, String ... extensions);

    /**
     *
     * @param rootPath
     * @param locatorClass
     */
    public void registerLocator(String rootPath, Class<? extends AssetLocator> locatorClass);

    /**
     *
     * @param listener
     */
    public void setAssetEventListener(AssetEventListener listener);

    /**
     * 
     * @param key
     * @return
     */
    public AssetInfo locateAsset(AssetKey<?> key);

    /**
     * Load an asset from a key, the asset will be located
     * by one of the {@link AssetLocator} implementations provided in the
     * {@link AssetManager#registerLocator(java.lang.String, java.lang.Class) }
     * call. If located successfully, it will be loaded via the the appropriate
     * {@link AssetLoader} implementation based on the file's extension, as
     * specified in the call 
     * {@link AssetManager#registerLoader(java.lang.Class, java.lang.String[]) }.
     *
     * @param <T> The object type that will be loaded from the AssetKey instance.
     * @param key The AssetKey
     * @return The loaded asset, or null if it was failed to be located
     * or loaded.
     */
    public <T> T loadAsset(AssetKey<T> key);

    /**
     * Load a named asset by name.
     * @param name
     * @return
     */
    public Object loadAsset(String name);

    /**
     * Loads texture file, supported types are BMP, JPG, PNG, GIF,
     * TGA and DDS.
     *
     * @param key
     * @return
     */
    public Texture loadTexture(TextureKey key);

    /**
     * Loads texture file, supported types are BMP, JPG, PNG, GIF,
     * TGA and DDS.
     *
     * @param name
     * @return
     */
    public Texture loadTexture(String name);

    /**
     * Load audio file, supported types are WAV or OGG.
     * @param key
     * @return
     */
    public AudioData loadAudio(AudioKey key);

    /**
     * Load audio file, supported types are WAV or OGG.
     * The file is loaded without stream-mode.
     * @param name
     * @return
     */
    public AudioData loadAudio(String name);

    /**
     * Loads a named model. Models can be jME3 object files (J3O) or
     * OgreXML/OBJ files.
     * @param key
     * @return
     */
    public Spatial loadModel(ModelKey key);

    /**
     * Loads a named model. Models can be jME3 object files (J3O) or
     * OgreXML/OBJ files.
     * @param name
     * @return
     */
    public Spatial loadModel(String name);

    /**
     * Load a material (J3M) file.
     * @param name
     * @return
     */
    public Material loadMaterial(String name);

    /**
     * Loads shader file(s), shouldn't be used by end-user in most cases.
     */
    public Shader loadShader(ShaderKey key);

    /**
     * Load a font file. Font files are in AngelCode text format,
     * and are with the extension "fnt".
     *
     * @param name
     * @return
     */
    public BitmapFont loadFont(String name);
}
