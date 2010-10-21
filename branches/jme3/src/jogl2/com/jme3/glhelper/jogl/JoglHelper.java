package com.jme3.glhelper.jogl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.jme3.glhelper.Helper;
import com.jme3.math.ColorRGBA;
import com.jme3.shader.Shader;

public class JoglHelper implements Helper {
	
	
	public JoglHelper(){
		//TODO: get the current GL and choose the best fitted delegate depending on the hardware (Desktop, Embedded)
	}

	@Override
    public void useProgram(int program) {
        GLContext.getCurrentGL().getGL2().glUseProgram(program);
    }
	
	@Override
	public void setMatrixMode(MatrixMode matrixMode){
		GLContext.getCurrentGL().getGL2().glMatrixMode(matrixMode.getGLConstant());
	}
	
	@Override
	public void loadMatrixf(FloatBuffer m){
		GLContext.getCurrentGL().getGL2().glLoadMatrixf(m);
	}
	
	@Override
	public void multMatrixf(FloatBuffer m){
		GLContext.getCurrentGL().getGL2().glMultMatrixf(m);
	}
	
	@Override
	public void setViewPort(int x, int y, int width, int height){
		GLContext.getCurrentGL().glViewport(x, y, width, height);
	}
	
	@Override
	public int getTexture0(){
		return GL.GL_TEXTURE0;
	}
	
	@Override
	public void setBackgroundColor(ColorRGBA color){
		GLContext.getCurrentGL().glClearColor(color.r, color.g, color.b, color.a);
	}
	
	@Override
	public void clear(BufferBit bufferBit){
		GLContext.getCurrentGL().glClear(bufferBit.getGLConstant());
	}
	
	@Override
	public void setDepthRange(float start, float end) {
        GLContext.getCurrentGL().glDepthRange(start, end);
    }
	
	@Override
	public void setScissor(int x, int y, int width, int height){
		GLContext.getCurrentGL().glScissor(x, y, width, height);
	}
	
	@Override
	public int getUniformLocation(Shader shader,String name,ByteBuffer nameBuffer){
		return GLContext.getCurrentGL().getGL2ES2().glGetUniformLocation(shader.getId(),name);
	}
}
