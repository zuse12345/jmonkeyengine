//uniform float exposurePow;
//uniform float exposureCutoff;
uniform sampler2D m_Texture;
uniform sampler2D m_Tex32;
uniform sampler2D m_Tex64;
uniform sampler2D m_Tex128;
uniform sampler2D m_Tex256;

varying vec2 texCoord;



void main(void)
{
  
   vec4 colorRes=texture2D(m_Texture, texCoord);
   vec4 color32=texture2D(m_Tex32, texCoord);
   vec4 color64=texture2D(m_Tex64, texCoord);
   vec4 color128=texture2D(m_Tex128, texCoord);
   vec4 color256=texture2D(m_Tex256, texCoord);
   
   gl_FragColor =colorRes+color32+color64+color128+color256;
}

