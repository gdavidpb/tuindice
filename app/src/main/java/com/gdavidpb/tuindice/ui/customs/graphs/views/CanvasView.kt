package com.gdavidpb.tuindice.ui.customs.graphs.views

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.*
import com.gdavidpb.tuindice.ui.customs.graphs.models.CanvasObject
import com.gdavidpb.tuindice.utils.extensions.doOnUpdate
import com.gdavidpb.tuindice.utils.extensions.isPowerSaveMode
import com.gdavidpb.tuindice.utils.extensions.loadAttributes
import com.google.android.material.animation.MatrixEvaluator

abstract class CanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val relationTouch: Float

    private val tapZoom: Float
    private val minZoom: Float
    private val maxZoom: Float
    private val epsilonZoom: Float

    private val zoomRange: ClosedFloatingPointRange<Float>

    private val zoomInterpolator: TimeInterpolator
    private val moveInterpolator: TimeInterpolator
    private val resetInterpolator: TimeInterpolator

    private val centerX by lazy { width / 2f }
    private val centerY by lazy { height / 2f }

    private val objects = hashMapOf<CanvasObject, Boolean>()

    private val matrixAnimator: ValueAnimator

    init {
        val defaultMinZoom = resolveFloat(context, R.dimen.zoom_default_min)
        val defaultMaxZoom = resolveFloat(context, R.dimen.zoom_default_max)

        loadAttributes(R.styleable.CanvasView, attrs).apply {
            minZoom = getFloat(R.styleable.CanvasView_minZoom, defaultMinZoom)
            maxZoom = getFloat(R.styleable.CanvasView_maxZoom, defaultMaxZoom)

            zoomInterpolator = resolveInterpolator(context,
                    R.styleable.CanvasView_zoomInterpolator,
                    android.R.anim.overshoot_interpolator
            )

            moveInterpolator = resolveInterpolator(context,
                    R.styleable.CanvasView_moveInterpolator,
                    android.R.anim.overshoot_interpolator
            )

            resetInterpolator = resolveInterpolator(context,
                    R.styleable.CanvasView_resetInterpolator,
                    android.R.anim.accelerate_decelerate_interpolator
            )
        }.recycle()

        tapZoom = resolveFloat(context, R.dimen.zoom_tap)
        epsilonZoom = resolveFloat(context, R.dimen.zoom_epsilon)
        relationTouch = resolveFloat(context, R.dimen.relation_node_touch)

        matrixAnimator = ValueAnimator.ofObject(MatrixEvaluator(), Matrix(), Matrix()).apply {
            val timeAnimationCanvas = resolveInt(context, R.dimen.time_animation_canvas)

            duration = timeAnimationCanvas.toLong()

            doOnUpdate {
                val matrix = it.animatedValue as Matrix

                canvasMatrix.set(matrix)

                invalidate()
            }
        }

        zoomRange = minZoom..maxZoom
    }

    private val canvasMatrix by lazy {
        Matrix(initialMatrix)
    }

    private val initialMatrix by lazy {
        Matrix().apply {
            setTranslate(centerX, centerY)
            postScale(minZoom, minZoom, centerX, centerY)
        }
    }

    private val moveDetector by lazy {
        GestureDetector(context, MoveDetector())
    }

    private val scaleDetector by lazy {
        ScaleGestureDetector(context, ScaleDetector())
    }

    abstract fun onMove(x: Float, y: Float)
    abstract fun onZoom(factor: Float)
    abstract fun onTap(canvasObject: CanvasObject, x: Float, y: Float)

    override fun onDraw(canvas: Canvas) {
        canvas.setMatrix(canvasMatrix)

        objects.forEach { (canvasObject, _) ->
            val isVisible = !canvas.quickReject(canvasObject.path, Canvas.EdgeType.AA)

            objects[canvasObject] = isVisible

            if (isVisible) canvas.drawPath(canvasObject.path, canvasObject.paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val isCustomHandle = moveDetector.onTouchEvent(event) || scaleDetector.onTouchEvent(event)

        if (isCustomHandle) invalidate()

        return isCustomHandle || super.onTouchEvent(event)
    }

    open fun zoomTo(factor: Float, x: Float, y: Float) {
        val targetMatrix = Matrix(canvasMatrix).apply {
            val cx = centerX - x
            val cy = centerY - y
            val sx = factor / scaleX
            val sy = factor / scaleY

            postTranslate(cx, cy)
            postScale(sx, sy, centerX, centerY)
        }

        animateMatrix(targetMatrix = targetMatrix, timeInterpolator = zoomInterpolator)
    }

    open fun moveTo(x: Float, y: Float) {
        val targetMatrix = Matrix(canvasMatrix).apply {
            val cx = centerX - x
            val cy = centerY - y

            postTranslate(cx, cy)
        }

        animateMatrix(targetMatrix = targetMatrix, timeInterpolator = moveInterpolator)
    }

    open fun reset() {
        animateMatrix(targetMatrix = initialMatrix, timeInterpolator = resetInterpolator)
    }

    open fun clear() {
        objects.clear()
        invalidate()
    }

    open fun addObjects(vararg values: CanvasObject) {
        objects.putAll(values.associate { it to false })
        invalidate()
    }

    private fun animateMatrix(targetMatrix: Matrix, timeInterpolator: TimeInterpolator) {
        if (canvasMatrix == targetMatrix) return

        val isPowerSaveMode = context.isPowerSaveMode()

        if (isPowerSaveMode) {
            canvasMatrix.set(targetMatrix)
            invalidate()
            return
        }

        with(matrixAnimator) {
            if (isRunning) cancel()

            interpolator = timeInterpolator

            val initialMatrix = Matrix(canvasMatrix)

            setObjectValues(initialMatrix, targetMatrix)

            start()
        }
    }

    private fun findCanvasObject(x: Float, y: Float, bounds: RectF): CanvasObject? {
        return objects.entries.find { (obj, isVisible) ->
            if (isVisible) {
                computeCanvasObjectBounds(obj, bounds)

                bounds.contains(x, y)
            } else {
                false
            }
        }?.key
    }

    private fun computeCanvasObjectBounds(canvasObject: CanvasObject, bounds: RectF): RectF {
        canvasObject.path.computeBounds(bounds, true)

        return bounds.apply {
            transform(canvasMatrix)
            inset(relationTouch)
        }
    }

    private inner class MoveDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val cantHandle = e.pointerCount > 1

            if (cantHandle) return false

            val bounds = RectF()
            val foundObject = findCanvasObject(e.x, e.y, bounds)

            return if (foundObject != null) {
                val canTap = foundObject.isClickable

                if (canTap)
                    onTap(foundObject, e.x, e.y)

                canTap
            } else {
                false
            }
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val cantHandle = e.pointerCount > 1

            if (cantHandle) return false

            val bounds = RectF()
            val foundObject = findCanvasObject(e.x, e.y, bounds)

            return if (foundObject != null) {
                val canTap = foundObject.isClickable

                if (canTap)
                    zoomTo(factor = tapZoom, x = bounds.centerX(), y = bounds.centerY())

                canTap
            } else {
                moveTo(x = e.x, y = e.y)

                true
            }
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val cantHandle = e1.pointerCount > 1 || e2.pointerCount > 1

            if (cantHandle) return false

            canvasMatrix.postTranslate(-distanceX, -distanceY)

            onMove(x = canvasMatrix.translateX, y = canvasMatrix.translateY)

            return true
        }
    }

    private inner class ScaleDetector : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            val zoomFactor = (factor * canvasMatrix.scaleX)
            val x = detector.focusX
            val y = detector.focusY

            val canZoom = zoomRange.contains(zoomFactor, epsilonZoom)

            if (canZoom) {
                canvasMatrix.postScale(factor, factor, x, y)

                onZoom(factor = canvasMatrix.scaleX)
            }

            return canZoom
        }
    }
}