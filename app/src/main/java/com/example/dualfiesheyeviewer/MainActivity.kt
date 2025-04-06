package com.example.dualfiesheyeviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.content.Context
import android.opengl.GLSurfaceView

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.dualfiesheyeviewer.rendering.SphereGLRenderer

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: SphereGLRenderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private val touchScaleFactor = -0.1f // adjust as needed

    init {
        // Render the view only when there is a change in the drawing data
        //renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = SphereGLRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            renderer.zoomBy((1f - scaleFactor) * 2f) // scale to zoom delta
            return true
        }
    })


    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        if (event.pointerCount == 1 && !scaleDetector.isInProgress) {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dx = x - previousX
                    val dy = y - previousY
                    renderer.rotateSphere(dx * touchScaleFactor, dy * touchScaleFactor)
                }
            }

            previousX = x
            previousY = y
        }

        return true
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var gLView: GLSurfaceView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)
    }
}