package com.infinity.wallpaper

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class InfinityWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = InfinityEngine()

    inner class InfinityEngine : Engine() {
        private var glSurfaceView: GLEngineSurfaceView? = null
        private var renderer: InfinityRenderer? = null
        private var sensorListener: SensorListener? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            renderer = InfinityRenderer(this@InfinityWallpaperService)
            glSurfaceView = GLEngineSurfaceView(surfaceHolder)
            glSurfaceView?.setRenderer(renderer)

            sensorListener = SensorListener(this@InfinityWallpaperService) { x, y ->
                renderer?.offsetX = x
                renderer?.offsetY = y
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                sensorListener?.register()
                glSurfaceView?.onResume()
            } else {
                sensorListener?.unregister()
                glSurfaceView?.onPause()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            sensorListener?.unregister()
            glSurfaceView?.onDestroy()
        }

        internal inner class GLEngineSurfaceView(private val holder: SurfaceHolder) : GLSurfaceView(this@InfinityWallpaperService) {
            override fun getHolder(): SurfaceHolder = holder
            fun onDestroy() { super.onDetachedFromWindow() }
        }
    }

    class InfinityRenderer(private val context: Context) : GLSurfaceView.Renderer {
        @Volatile var offsetX: Float = 0f
        @Volatile var offsetY: Float = 0f
        private var program: Int = 0
        private var particleSystem: StarfieldParticleSystem? = null
        private val mvpMatrix = FloatArray(16)
        private val projectionMatrix = FloatArray(16)
        private val viewMatrix = FloatArray(16)

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(0.01f, 0.01f, 0.05f, 1.0f)
            val vertCode = ShaderUtils.loadShaderFromAssets(context, "shaders/starfield.vert")
            val fragCode = ShaderUtils.loadShaderFromAssets(context, "shaders/starfield.frag")
            program = ShaderUtils.createProgram(vertCode, fragCode)
            particleSystem = StarfieldParticleSystem(350)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / height.toFloat()
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
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
