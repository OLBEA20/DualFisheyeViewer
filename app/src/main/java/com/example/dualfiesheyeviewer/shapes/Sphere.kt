package com.example.dualfiesheyeviewer.shapes

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.*

class Sphere(
    private val textureId: Int,
    private val stacks: Int = 30,
    private val slices: Int = 30,
    private val radius: Float = 1f
) {
    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val vertexCount: Int
    private val indexCount: Int
    private val stride = 5 * 4 // (x, y, z, u, v) — 5 floats per vertex

    private val program: Int
    private val positionHandle: Int
    private val texCoordHandle: Int
    private val mvpMatrixHandle: Int
    private val textureUniformHandle: Int

    init {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        for (i in 0..stacks) {
            val phi = PI * i / stacks
            val y = cos(phi).toFloat()
            val r = sin(phi).toFloat()

            for (j in 0..slices) {
                val theta = 2.0 * PI * j / slices
                val x = r * cos(theta).toFloat()
                val z = r * sin(theta).toFloat()

                val u = j.toFloat() / slices
                val v = i.toFloat() / stacks

                vertices.addAll(listOf(x * radius, y * radius, z * radius, u, v))
            }
        }

        for (i in 0 until stacks) {
            for (j in 0 until slices) {
                val first = (i * (slices + 1) + j).toShort()
                val second = (first + slices + 1).toShort()

                //indices.addAll(listOf(first, second, (first + 1).toShort()))
                //indices.addAll(listOf(second, (second + 1).toShort(), (first + 1).toShort()))
                indices.addAll(listOf((first + 1).toShort(), second, first))
                indices.addAll(listOf((first + 1).toShort(), (second + 1).toShort(), second))

            }
        }

        vertexCount = vertices.size / 5
        indexCount = indices.size

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices.toShortArray())
                position(0)
            }

        // === SHADERS ===
        val vertexShaderCode = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uMVPMatrix;
            varying vec2 vTexCoord;

            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES uTexture;
        varying vec2 vTexCoord;
         
        vec3 psph;

        const float PI = 3.14159265359;
        const float FOV = 186.0 * PI / 180.0; 
        const float HALF_PI = PI * 0.5;


        void main() {
            float x_dest = vTexCoord.x < 0.25 ? vTexCoord.x + 0.5 : vTexCoord.x > 0.75 ? vTexCoord.x - 0.5 : vTexCoord.x; 
            float y_dest = vTexCoord.y; 

            float theta = 2.0 * PI * (x_dest - 0.5);
            float phi = PI * (y_dest - 0.5);

            psph.x = cos(phi) * sin(theta);
            psph.y = cos(phi) * cos(theta);
            psph.z = sin(phi);

            theta = atan(psph.z, psph.x);
            phi = atan(sqrt(psph.x * psph.x + psph.z * psph.z), psph.y);
            float r = phi / FOV;

            vec2 pfish;
            pfish.x = vTexCoord.x > 0.25 && vTexCoord.x < 0.75 ? (0.5 + r * cos(theta)) / 2.0 : (1.5 + r * cos(theta)) / 2.0;
            pfish.y = 0.5 + r * sin(theta);

            gl_FragColor = texture2D(uTexture, pfish);
        }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        textureUniformHandle = GLES20.glGetUniformLocation(program, "uTexture")
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        // Vertex positions
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)

        // Texture coordinates
        vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer)

        // Pass MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Bind video texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(textureUniformHandle, 0)

        // Draw
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        // Clean up
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
