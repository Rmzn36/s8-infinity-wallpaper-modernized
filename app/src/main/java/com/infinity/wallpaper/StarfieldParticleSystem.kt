package com.infinity.wallpaper

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Random

class StarfieldParticleSystem(private val particleCount: Int = 750) {

    private val vertexBuffer: FloatBuffer
    private val pointSizeBuffer: FloatBuffer
    private val alphaBuffer: FloatBuffer

    init {
        val random = Random()
        val vertices = FloatArray(particleCount * 3)
        val sizes = FloatArray(particleCount)
        val alphas = FloatArray(particleCount)

        for (i in 0 until particleCount) {
            vertices[i * 3] = (random.nextFloat() * 2.4f) - 1.2f
            vertices[i * 3 + 1] = (random.nextFloat() * 2.4f) - 1.2f
            vertices[i * 3 + 2] = random.nextFloat() * 4.0f + 0.8f

            // Farklı derinliklerdeki yıldız boyutları (6px - 22px)
            sizes[i] = random.nextFloat() * 16f + 6f
            alphas[i] = random.nextFloat() * 0.85f + 0.15f
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        vertexBuffer.position(0)

        pointSizeBuffer = ByteBuffer.allocateDirect(sizes.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(sizes)
        pointSizeBuffer.position(0)

        alphaBuffer = ByteBuffer.allocateDirect(alphas.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(alphas)
        alphaBuffer.position(0)
    }

    fun draw(aPositionLoc: Int, aPointSizeLoc: Int, aAlphaLoc: Int) {
        GLES20.glEnableVertexAttribArray(aPositionLoc)
        GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(aPointSizeLoc)
        GLES20.glVertexAttribPointer(aPointSizeLoc, 1, GLES20.GL_FLOAT, false, 0, pointSizeBuffer)

        GLES20.glEnableVertexAttribArray(aAlphaLoc)
        GLES20.glVertexAttribPointer(aAlphaLoc, 1, GLES20.GL_FLOAT, false, 0, alphaBuffer)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleCount)
    }
}
