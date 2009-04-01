package com.g3d.scene.material;

import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.shader.Shader;

public class PhongMaterial extends Material {

    public PhongMaterial(float shiny){
        shader = new Shader();
        shader.addSource(Shader.ShaderType.Vertex,
                "#version 120\n"+
                "uniform vec4 g_LightColor[4];\n" +
                "uniform vec4 g_LightPosition[4];\n" +
                "uniform mat4 g_WorldViewProjectionMatrix;" +
                "uniform mat4 g_WorldMatrix;\n" +
                "uniform mat4 g_WorldViewMatrix;\n" +
                "uniform mat4 g_ViewMatrix;\n" +
                "in vec3 inPosition;\n" +
                "in vec3 inNormal;\n" +
                "varying vec3 normal;\n" +
                "varying vec4 lightDir[4];\n" +
                "varying vec3 viewDir;\n" +
                "\n" +
                "void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightOut){\n"+
                "   float posLight = step(0.5, color.w);\n" +
                "   vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);\n" +
                "   float dist = length(tempVec);\n" +
                "   lightOut.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);\n" +
                "   lightOut.xyz = tempVec / dist;\n" +
                "}\n" +
                "\n"+
                "void main(){\n" +
                "   gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition,1.0);\n" +
                "   vec3 worldPos = (g_WorldMatrix * vec4(inPosition,1.0)).xyz;\n" +
                "   normal = normalize( mat3(g_WorldMatrix) * inNormal );\n" +
                "   viewDir = -normalize(g_WorldViewMatrix * vec4(inPosition,1.0)).xyz;\n" +
                "   // compute for each light\n"+
                "   for (int i = 0; i < 4; i++){\n" +
                "       lightComputeDir(worldPos, g_LightColor[i], g_LightPosition[i], lightDir[i]);\n" +
                "   }\n" +
                "}\n");
        shader.addSource(Shader.ShaderType.Fragment,
                "#version 120\n"+
                "uniform float g_Shininess;\n" +
                "uniform vec4 g_LightColor[4];\n"+
                "in vec3 normal;\n" +
                "in vec4 lightDir[4];\n" +
                "in vec3 viewDir;\n" +
                "\n"+
                "vec3 lightComputeDiffuse(in vec3 norm, in vec4 lightdir, in vec4 color){\n" +
                "    return vec3(max(0.0, dot(normal, lightdir.xyz))) * color.xyz * lightdir.w;\n" +
                "}\n" +
                "void main(){\n" +
                "   //vec3 R = -reflect(lightDir, normal);\n" +
                "   //float RdotV = max(0.0, dot(R, viewDir));\n" +
                "   //float specular = max(0.0, pow(dot(R, viewDir), shiny));\n" +
                "   vec3 sum_diffuse = vec3(0.0);\n"+
                "   for (int i = 0; i < 4; i++){\n" +
                "        sum_diffuse += lightComputeDiffuse(normal, lightDir[i], g_LightColor[i]);\n"+
                "   }\n"+
                "   //vec3 R = -reflect(viewDir, normal);\n" +
                "   //float sum_spec = max(0.0, pow(dot(R, lightDir), g_Shininess));\n" +
                "   //float intensity = 1.0/pi + 1.0 * (g_Shininess + 2.0) * sum_spec/(2.0*pi);\n" +
                "   //gl_FragColor = vec4(intensity * NdotL);\n" +
                "   gl_FragColor = vec4(sum_diffuse,1.0);\n" +
                "}\n");
        setShininess(shiny);
    }

    public void setShininess(float shiny){
        if (shiny < 1f || shiny > 128f)
            throw new IllegalArgumentException("Shininess must be between 1 and 128");

        shader.getUniform("g_Shininess").setFloat(shiny);
    }

    @Override
    public void apply(Geometry g, Renderer r){
        super.apply(g, r);
        r.updateLightListUniforms(shader, g);
    }

}
