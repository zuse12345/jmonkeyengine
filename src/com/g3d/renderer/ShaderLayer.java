/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.renderer;

import com.g3d.shader.Shader;
import com.g3d.shader.Shader.ShaderSource;

/**
 * Renderer layer for handling shaders
 */
public interface ShaderLayer {

    /**
     * Updates the shader source, creating an ID and registering
     * with the object manager.
     * @param source
     */
    public void updateShaderSourceData(ShaderSource source);

    /**
     * Uploads the shader source code and prepares it for use.
     * @param shader
     */
    public void updateShaderData(Shader shader);

    /**
     * @param shader Sets the shader to use for rendering, uploading it
     * if neccessary.
     */
    public void setShader(Shader shader);

    /**
     * @param shader The shader to delete. This method also deletes
     * the attached shader sources.
     */
    public void deleteShader(Shader shader);

    /**
     * Deletes the provided shader source.
     * @param source
     */
    public void deleteShaderSource(ShaderSource source);

}
