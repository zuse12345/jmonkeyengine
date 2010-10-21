package com.jme3.glhelper.jogl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.jme3.glhelper.Helper;

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
}
