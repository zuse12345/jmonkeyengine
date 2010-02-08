/*
 * Copyright (c) 2003-2009 jMonkeyEngine
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

package com.g3d.animation.tbdeleted;

public class BoneAnimationLoader {

//    public static String applySkinningShader(String shader, int numBones, int maxWeightsPerVert){
//        shader = shader.replace("hw_skin_vars", "attribute vec4 weights;\n" +
//                                       "attribute vec4 indexes;\n" +
//                                       "uniform mat4 boneMatrices["+numBones+"];\n");
//        if (maxWeightsPerVert == 1){
//            shader = shader.replace("hw_skin_compute",
//                                      "    vec4 vPos = boneMatrices[int(indexes.x)] * gl_Vertex;\n" +
//                                      "\n");
//        }else{
//            shader = shader.replace("hw_skin_compute",
//                                          "    vec4 index = indexes;\n" +
//                                          "    vec4 weight = weights;\n" +
//                                          "\n" +
//                                          "    vec4 vPos = vec4(0.0);\n" +
//                                          "    vec4 vNormal = vec4(0.0);\n" +
//                                          "    vec4 normal = vec4(gl_Normal.xyz,0.0);\n" +
//                                          "\n" +
//                                          "    for (float i = 0.0; i < "+((float)maxWeightsPerVert)+"; i += 1.0){\n" +
//                                          "        mat4 skinMat = boneMatrices[int(index.x)];\n" +
//                                          "        vPos    += weight.x * (skinMat * gl_Vertex);\n" +
//                                          "        vNormal += weight.x * (skinMat * normal);\n" +
//                                          "        index = index.yzwx;\n" +
//                                          "        weight = weight.yzwx;\n" +
//                                          "    }\n" +
//                                          "\n");
//        }
//        shader = shader.replace("hw_skin_vpos", "(gl_ModelViewProjectionMatrix * vPos)");
//        shader = shader.replace("hw_skin_vnorm", "(normalize(inverseModelView * tempNormal).xyz)");
//
//        return shader;
//    }

//    public static GLSLShaderObjectsState createSkinningShader(int numBones, int maxWeightsPerVert){
//        GLSLShaderObjectsState shader = DisplaySystem.getDisplaySystem().getRenderer().createGLSLShaderObjectsState();
//        String str =    "hw_skin_vars\n" +
//                        "\n" +
//                        "void main(){\n" +
//                        "   hw_skin_compute;\n" +
//                        "\n" +
//                        "   gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
//                        "   gl_FrontColor = gl_LightSource[0].ambient;\n" +
//                        "   //vPos = gl_Vertex;\n" +
//                        "   gl_Position = hw_skin_vpos;\n" +
//                        "}\n";
//        //hw_skin_compute
//        //hw_skin_vars
//        str = applySkinningShader(str, numBones, maxWeightsPerVert);
//        //System.out.println(str);
//        shader.load(str, null);
//        return shader;
//    }

}
