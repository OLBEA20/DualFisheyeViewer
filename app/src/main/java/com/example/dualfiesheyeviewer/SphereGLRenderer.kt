package com.example.dualfiesheyeviewer

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.Surface
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SphereGLRenderer(private val context: Context): GLSurfaceView.Renderer {
    private lateinit var sphere: DualFisheyeSphere
    private val projMatrix = FloatArray(16)

    private lateinit var surfaceTexture: SurfaceTexture
    private lateinit var mediaPlayer: MediaPlayer
    private var textureId: Int = 0


    override fun onSurfaceCreated(
        unused: GL10?,
        config: EGLConfig?
    ) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        surfaceTexture = SurfaceTexture(textureId)

        mediaPlayer = MediaPlayer.create(context, R.raw.my_video)
        mediaPlayer.setSurface(Surface(surfaceTexture))
        mediaPlayer.isLooping = true
        mediaPlayer.start()


        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        sphere = DualFisheyeSphere(textureId)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        surfaceTexture.updateTexImage()

        val viewMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        val rotationMatrixX = FloatArray(16)
        val rotationMatrixY = FloatArray(16)
        val tempMatrix = FloatArray(16)

        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, zoom,
            0f, 0f, 0f,
            0f, 1f, 0f)

        Matrix.setRotateM(rotationMatrixY, 0, yaw, 0f, 1f, 0f)
        Matrix.setRotateM(rotationMatrixX, 0, pitch, 1f, 0f, 0f)
        Matrix.multiplyMM(tempMatrix, 0, rotationMatrixY, 0, rotationMatrixX, 0)
        Matrix.multiplyMM(viewMatrix, 0, viewMatrix, 0, tempMatrix, 0)

        val scaleMatrix = FloatArray(16)
        Matrix.setIdentityM(scaleMatrix, 0)
        Matrix.scaleM(scaleMatrix, 0, 6f, 6f, 6f)
        Matrix.multiplyMM(viewMatrix, 0, viewMatrix, 0, scaleMatrix, 0)

        val ratio = width.toFloat() / height
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 0.5f, 100f - zoom)

        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        sphere.draw(mvpMatrix)
    }

    private var width = 0
    private var height = 0

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height

        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 10f)
    }


    private var yaw = 0f
    private var pitch = 0f

    fun rotateSphere(deltaYaw: Float, deltaPitch: Float) {
        yaw += deltaYaw
        pitch += deltaPitch.coerceIn(-90f, 90f)
    }

    private var zoom = 1f
    private val minZoom = 0.1f
    private val maxZoom = 5f

    fun zoomBy(delta: Float) {
        zoom = (zoom + delta).coerceIn(minZoom, maxZoom)
    }
}