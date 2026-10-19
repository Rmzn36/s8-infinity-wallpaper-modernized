attribute vec4 a_Position;
attribute float a_PointSize;
attribute float a_Alpha;

uniform mat4 u_MVPMatrix;
uniform vec2 u_Offset;

varying float v_Alpha;

void main() {
    vec4 pos = a_Position;
    pos.x += u_Offset.x * (pos.z * 0.15);
    pos.y += u_Offset.y * (pos.z * 0.15);

    gl_Position = u_MVPMatrix * pos;
    gl_PointSize = a_PointSize;
    v_Alpha = a_Alpha;
}
