package com.infinity.wallpaper

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

class StarfieldParticleSystem(private val particleCount: Int = 1200) {

    private val vertexBuffer: FloatBuffer
    private val pointSizeBuffer: FloatBuffer
    private val alphaBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer

    init {
        val random = Random()
        val vertices = FloatArray(particleCount * 3)
        val sizes = FloatArray(particleCount)
        val alphas = FloatArray(particleCount)
        val colors = FloatArray(particleCount * 3)

        val palette = arrayOf(
            floatArrayOf(0.55f, 0.75f, 1.0f),  // S8 Mavi
            floatArrayOf(0.75f, 0.55f, 1.0f),  // S8 Mor
            floatArrayOf(0.45f, 0.90f, 0.9f),  // Turkuaz
            floatArrayOf(1.0f, 1.0f, 1.0f)     // Beyaz
        )

        for (i in 0 until particleCount) {
            // S8 Infinity tarzı merkez/diagonal eksenel yoğunlaşma
            val radius = random.nextFloat() * random.nextFloat() * 2.2f
            val angle = random.nextFloat() * Math.PI.toFloat() * 2f

            val posX = cos(angle) * radius * 0.8f + (random.nextFloat() - 0.5f) * 0.6f
            val posY = sin(angle) * radius * 1.4f + (random.nextFloat() - 0.5f) * 0.6f
            val posZ = random.nextFloat() * 3.5f + 0.8f

            vertices[i * 3] = posX
            vertices[i * 3 + 1] = posY
            vertices[i * 3 + 2] = posZ

            // Daha kibar ve estetik boyutlar (3px - 12px)
            sizes[i] = random.nextFloat() * 9f + 3f
            alphas[i] = random.nextFloat() * 0.75f + 0.25f

            val color = palette[random.nextInt(palette.size)]
            colors[i * 3] = color[0]
            colors[i * 3 + 1] = color[1]
            colors[i * 3 + 2] = color[2]
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        vertexBuffer.position(0)

        pointSizeBuffer = ByteBuffer.allocateDirect(sizes.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(sizes)
        pointSizeBuffer.position(0)

        alphaBuffer = ByteBuffer.allocateDirect(alphas.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(alphas)
        alphaBuffer.position(0)

        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(colors)
        colorBuffer.position(0)
    }

    fun draw(aPositionLoc: Int, aPointSizeLoc: Int, aAlphaLoc: Int, aColorLoc: Int) {
        GLES20.glEnableVertexAttribArray(aPositionLoc)
        GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(aPointSizeLoc)
        GLES20.glVertexAttribPointer(aPointSizeLoc, 1, GLES20.GL_FLOAT, false, 0, pointSizeBuffer)

        GLES20.glEnableVertexAttribArray(aAlphaLoc)
        GLES20.glVertexAttribPointer(aAlphaLoc, 1, GLES20.GL_FLOAT, false, 0, alphaBuffer)

        if (aColorLoc != -1) {
            GLES20.glEnableVertexAttribArray(aColorLoc)
            GLES20.glVertexAttribPointer(aColorLoc, 3, GLES20.GL_FLOAT, false, 0, colorBuffer)
        }

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleCount)
    }
}
