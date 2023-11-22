#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 iResolution;
uniform float iTime;
uniform vec2 iMouse;

#define PI 0.01

void main( void ) {

    float speed = iTime * .1;
    float cycle = clamp(iTime, 999.0,9999.0);
    vec2 uPos = ( gl_FragCoord.xy / iResolution.xy );
    uPos.y -= 0.50;
    uPos.y += (tan( uPos.x * cycle + speed ) - tan( uPos.x * 10000. + speed )) * .02;

    float dy = 0.1/ ( 50. * abs(uPos.y));

    gl_FragColor = vec4( (uPos.x + 0.0) * dy, 0.1 * dy, dy+=0.0, 1.0 );
}
