package com.example.dualfiesheyeviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.content.Context
import android.opengl.GLSurfaceView

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.dualfiesheyeviewer.rendering.MyGLRenderer
import com.example.dualfiesheyeviewer.rendering.SphereGLRenderer

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f

    private val renderer: SphereGLRenderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private val touchScaleFactor = 0.5f // adjust as needed

    init {
        // Render the view only when there is a change in the drawing data
        //renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = SphereGLRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }


    //override fun onTouchEvent(e: MotionEvent): Boolean {
    //    // MotionEvent reports input details from the touch screen
    //    // and other input controls. In this case, you are only
    //    // interested in events where the touch position changed.

    //    val x: Float = e.x
    //    val y: Float = e.y

    //    when (e.action) {
    //        MotionEvent.ACTION_MOVE -> {

    //            var dx: Float = x - previousX
    //            var dy: Float = y - previousY

    //            // reverse direction of rotation above the mid-line
    //            if (y > height / 2) {
    //                dx *= -1
    //            }

    //            // reverse direction of rotation to left of the mid-line
    //            if (x < width / 2) {
    //                dy *= -1
    //            }

    //            //renderer.angle += (dx + dy) * TOUCH_SCALE_FACTOR
    //            requestRender()
    //        }
    //    }

    //    previousX = x
    //    previousY = y
    //    return true
    //}
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