uniform float m_ExposurePow;
uniform float m_ExposureCutoff;
uniform sampler2D m_Texture;
varying vec2 texCoord;
 
void main(void)
{ 
   vec4 color = texture2D( m_Texture, texCoord );
   if ( (color.r+color.g+color.b)/3.0 < m_ExposureCutoff ) {
      color = vec4(0.0);
   }else{
      color = pow(color,vec4(m_ExposurePow));
   }
  
   gl_FragColor = color;
}
