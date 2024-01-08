uniform float iTime;
uniform vec2 iResolution;

void main( void ) {
    vec2 uv = ( gl_FragCoord.xy + 10 * iResolution.xy ) / iResolution.y + 10.0 ;
    float X = uv.x * 64.0;
    float Y = -uv.y * 64.0;
    float c = sin( X / 10.0 + Y / 15.0 ) * cos( X / 20.0 + ( iTime * 2.0 ) + cos( 0.1 * (iTime * 2.0) + Y / 5.0 ));
    vec3 col = 0.2 + 0.4 * cos( iTime + uv.xyx + vec3(0, 2, 4));
    gl_FragColor = vec4(col, 1.0);
}
