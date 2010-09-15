uniform sampler2D Alpha;
uniform sampler2D Texture1;
uniform sampler2D Texture2;
uniform sampler2D Texture3;
uniform float Texture1Scale;
uniform float Texture2Scale;
uniform float Texture3Scale;

varying vec2 texCoord;

void main(void)
{
	vec4 alpha   = texture2D( Alpha, texCoord.xy );
	vec4 tex0    = texture2D( Texture1, texCoord.xy * Texture1Scale ); // Tile
	vec4 tex1    = texture2D( Texture2, texCoord.xy * Texture2Scale ); // Tile
	vec4 tex2    = texture2D( Texture3, texCoord.xy * Texture3Scale ); // Tile

	tex0 *= alpha.r; // Red channel
	tex1 = mix( tex0, tex1, alpha.g ); // Green channel
	vec4 outColor = mix( tex1, tex2, alpha.b ); // Blue channel

	gl_FragColor = outColor;
}

