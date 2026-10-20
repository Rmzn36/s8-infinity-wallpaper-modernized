package com.infinity.wallpaper

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import javax.microedition.khronos.egl.*

class InfinityWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = InfinityEngine()

    inner class InfinityEngine : Engine(), Runnable {
        private var egl: EGL10? = null
        private var eglDisplay: EGLDisplay? = null
        private var eglContext: EGLContext? = null
        private var eglSurface: EGLSurface? = null

        @Volatile private var isRunning = false
        private var renderThread: Thread? = null

        private var renderer: InfinityRenderer? = null
        private var sensorListener: SensorListener? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            renderer = InfinityRenderer(this@InfinityWallpaperService)
            sensorListener = SensorListener(this@InfinityWallpaperService) { x, y ->
                renderer?.offsetX = x
                renderer?.offsetY = y
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                sensorListener?.register()
                startRenderThread()
            } else {
                sensorListener?.unregister()
                stopRenderThread()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopRenderThread()
        }

        private fun startRenderThread() {
            if (isRunning) return
            isRunning = true
            renderThread = Thread(this, "GLRenderThread").apply { start() }
        }

        private fun stopRenderThread() {
            isRunning = false
            try {
                renderThread?.join(500)
            } catch (e: Exception) { }
            renderThread = null
        }

        override fun run() {
            initGL()
            val surfaceHolder = surfaceHolder
            val frame = surfaceHolder.surfaceFrame
            renderer?.onSurfaceCreated()
            renderer?.onSurfaceChanged(frame.width(), frame.height())

            while (isRunning) {
                if (eglDisplay != null && eglSurface != null) {
                    renderer?.onDrawFrame()
                    egl?.eglSwapBuffers(eglDisplay, eglSurface)
                }
                try {
                    Thread.sleep(16) // ~60 FPS
                } catch (e: Exception) { }
            }

            cleanupGL()
        }

        private fun initGL() {
            egl = EGLContext.getEGL() as EGL10
            eglDisplay = egl?.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            val version = IntArray(2)
            egl?.eglInitialize(eglDisplay, version)

            val configSpec = intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_NONE
            )

            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            egl?.eglChooseConfig(eglDisplay, configSpec, configs, 1, numConfig)

            val attribList = intArrayOf(0x3098, 2, EGL10.EGL_NONE) // EGL_CONTEXT_CLIENT_VERSION = 2
            eglContext = egl?.eglCreateContext(eglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, attribList)

            eglSurface = egl?.eglCreateWindowSurface(eglDisplay, configs[0], surfaceHolder, null)
            egl?.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        }

        private fun cleanupGL() {
            if (eglDisplay != null) {
                egl?.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
                if (eglSurface != null) egl?.eglDestroySurface(eglDisplay, eglSurface)
                if (eglContext != null) egl?.eglDestroyContext(eglDisplay, eglContext)
                egl?.eglTerminate(eglDisplay)
            }
        }
    }

    class InfinityRenderer(private val context: Context) {
        @Volatile var offsetX: Float = 0f
        @Volatile var offsetY: Float = 0f
        private var program: Int = 0
        private var particleSystem: StarfieldParticleSystem? = null
        private val mvpMatrix = FloatArray(16)
        private val projectionMatrix = FloatArray(16)
        private val viewMatrix = FloatArray(16)

        private val vertexShaderCode = """
            attribute vec4 a_Position;
            attribute float a_PointSize;
            attribute float a_Alpha;
            uniform mat4 u_MVPMatrix;
            uniform vec2 u_Offset;
            varying float v_Alpha;
            void main() {
                vec4 pos = a_Position;
                pos.x += u_Offset.x * (pos.z * 0.12);
                pos.y += u_Offset.y * (pos.z * 0.12);
                gl_Position = u_MVPMatrix * pos;
                gl_PointSize = a_PointSize;
                v_Alpha = a_Alpha;
            }
        """.trimIndent()

        private val fragmentShaderCode = """
            precision mediump float;
            varying float v_Alpha;
            void main() {
                vec2 coord = gl_PointCoord - vec2(0.5);
                float dist = length(coord);
                if (dist > 0.5) { discard; }
                float core = smoothstep(0.5, 0.0, dist);
                vec3 starColor = mix(vec3(0.6, 0.8, 1.0), vec3(1.0, 1.0, 1.0), core);
                gl_FragColor = vec4(starColor, v_Alpha * core);
            }
        """.trimIndent()

        fun onSurfaceCreated() {
            GLES20.glClearColor(0.02f, 0.02f, 0.08f, 1.0f)
            program = ShaderUtils.createProgram(vertexShaderCode, fragmentShaderCode)
            particleSystem = StarfieldParticleSystem(650)
        }

        fun onSurfaceChanged(width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / if (height == 0) 1f else height.toFloat()
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        }

        fun onDrawFrame() {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            if (program == 0) return

            GLES20.glUseProgram(program)

            val uMVPMatrixLoc = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
            val uOffsetLoc = GLES20.glGetUniformLocation(program, "u_Offset")
            val aPositionLoc = GLES20.glGetAttribLocation(program, "a_Position")
            val aPointSizeLoc = GLES20.glGetAttribLocation(program, "a_PointSize")
            val aAlphaLoc = GLES20.glGetAttribLocation(program, "a_Alpha")

            GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)
            GLES20.glUniform2f(uOffsetLoc, offsetX, offsetY)

            particleSystem?.draw(aPositionLoc, aPointSizeLoc, aAlphaLoc)
        }
    }
}
