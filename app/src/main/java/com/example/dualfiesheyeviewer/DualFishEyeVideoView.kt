package com.example.dualfiesheyeviewer

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class DualFishEyeVideoView(context: Context) : GLSurfaceView(context) {
    private val renderer: SphereGLRenderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private val touchScaleFactor = -0.1f // adjust as needed

    init {
        //renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        setEGLContextClientVersion(2)

        renderer = SphereGLRenderer(context)
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