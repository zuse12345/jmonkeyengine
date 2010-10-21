package com.jme3.glhelper;

import java.nio.FloatBuffer;

public interface Helper {
	
	public enum MatrixMode{
			
		MODELVIEW(0x1700),PROJECTION(0x1701);
		
		private final int glConstant;
		
		private MatrixMode(int glConstant){
			this.glConstant = glConstant;
		}
		
		public final int getGLConstant(){
			return glConstant;
		}
	};
	
	public enum Bit{
		COLOR_BUFFER(16384),DEPTH_BUFFER(256);
		
		private final int glConstant;
		
		private Bit(int glConstant){
			this.glConstant = glConstant;
		}
		
		public final int getGLConstant(){
			return glConstant;
		}
	}
	
	public enum Filter{
		NEAREST(9728);
		
        private final int glConstant;
		
		private Filter(int glConstant){
			this.glConstant = glConstant;
		}
		
		public final int getGLConstant(){
			return glConstant;
		}
	}

	public void useProgram(int program);
	
	public void setMatrixMode(MatrixMode matrixMode);
	
	public void loadMatrixf(FloatBuffer m);
	
	public void multMatrixf(FloatBuffer m);
	
	public void setViewPort(int x, int y, int width, int height);
	
	public int getTexture0();
}
