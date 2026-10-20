precision mediump float;

varying float v_Alpha;

void main() {
    vec2 coord = gl_PointCoord - vec2(0.5);
    float dist = length(coord);
    
    if (dist > 0.5) {
        discard;
    }
    
    // Merkezde yoğun parlaklık, kenarlarda yumuşak ışık halesi (Glow)
    float core = smoothstep(0.5, 0.0, dist);
    float glow = pow(core, 2.0);
    
    // S8 Infinity tarzı hafif mavi-beyaz uzay tonu
    vec3 starColor = mix(vec3(0.65, 0.82, 1.0), vec3(1.0, 1.0, 1.0), core);
    
    gl_FragColor = vec4(starColor, v_Alpha * (core + glow * 0.5));
}
