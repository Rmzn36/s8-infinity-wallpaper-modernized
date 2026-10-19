precision mediump float;

varying float v_Alpha;

void main() {
    vec2 coord = gl_PointCoord - vec2(0.5);
    float dist = length(coord);
    
    if (dist > 0.5) {
        discard;
    }
    
    float intensity = 1.0 - (dist * 2.0);
    gl_FragColor = vec4(0.8, 0.9, 1.0, v_Alpha * intensity);
}
