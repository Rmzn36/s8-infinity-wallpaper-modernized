        private val vertexShaderCode = """
            attribute vec4 a_Position;
            attribute float a_PointSize;
            attribute float a_Alpha;
            attribute vec3 a_Color;
            
            uniform mat4 u_MVPMatrix;
            uniform vec2 u_Offset;
            uniform float u_Time;
            
            varying float v_Alpha;
            varying vec3 v_Color;
            
            void main() {
                vec4 pos = a_Position;
                
                // Hassasiyeti düşürülmüş yumuşak paralaks hareketi
                pos.x += u_Offset.x * (pos.z * 0.08);
                pos.y += u_Offset.y * (pos.z * 0.08);
                
                gl_Position = u_MVPMatrix * pos;
                
                float twinkle = sin(u_Time * 1.5 + pos.x * 8.0) * 0.2 + 0.8;
                gl_PointSize = a_PointSize * twinkle;
                
                v_Alpha = a_Alpha * twinkle;
                v_Color = a_Color;
            }
        """.trimIndent()
