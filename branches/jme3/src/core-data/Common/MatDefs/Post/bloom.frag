//uniform float exposurePow;
//uniform float exposureCutoff;
uniform sampler2D m_Texture;
uniform float m_Size;
varying vec2 texCoord;


float[7] sample=float[7](-3.0,-2.0,-1.0,0.0,1.0,2.0,3.0);
float[7] weight=float[7](0.015625,0.09375,0.234375,0.3125,0.234375,0.09375,0.015625);

void main(void)
{   vec2 ScaleU=vec2(1.0/m_Size);
    float exposureCutoff=0.8;
    float exposurePow=1.0;
 //   vec4 colorRes=texture2D(m_Texture, texCoord);
    vec4 sum = vec4(0.0);
    int i=0;
    for(i=0;i<7;i++){
       vec4 color = texture2D( m_Texture, texCoord + vec2(0.0, sample[i]*ScaleU ) );
       if ( (color.r+color.g+color.b)/3.0 < exposureCutoff ) {
          color = vec4(0.0);
       }else{
          color = pow(color,vec4(exposurePow));
          color *= weight[i];
       }
       sum+=color;
    }
   gl_FragColor = sum;//mix(sum , colorRes, 0.6);
}
