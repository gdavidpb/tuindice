package com.gdavidpb.tuindice.ui.custom.graphs.views

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.utils.extension.getInterpolator
import com.gdavidpb.tuindice.base.utils.extension.loadAttributes
import com.gdavidpb.tuindice.ui.custom.graphs.extensions.*
import com.google.android.material.animation.MatrixEvaluator

abstract class CanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {

	private object Defaults {
		const val MIN_ZOOM = .3f
		const val MAX_ZOOM = 3f
		const val EPSILON_ZOOM = .01f

		const val TIME_ANIMATION = 750
	}

	private val minZoom: Float
	private val maxZoom: Float
	private val epsilonZoom: Float

	private val zoomRange: ClosedFloatingPointRange<Float>

	private val zoomInterpolator: TimeInterpolator
	private val moveInterpolator: TimeInterpolator
	private val resetInterpolator: TimeInterpolator

	private val centerX by lazy { width / 2f }
	private val centerY by lazy { height / 2f }

	private val matrixAnimator: ValueAnimator

	init {
		loadAttributes(R.styleable.CanvasView, attrs).apply {
			minZoom = getFloat(R.styleable.CanvasView_minZoom, Defaults.MIN_ZOOM)
			maxZoom = getFloat(R.styleable.CanvasView_maxZoom, Defaults.MAX_ZOOM)

			zoomInterpolator = getInterpolator(
				context,
				R.styleable.CanvasView_zoomInterpolator,
				android.R.anim.overshoot_interpolator
			)

			moveInterpolator = getInterpolator(
				context,
				R.styleable.CanvasView_moveInterpolator,
				android.R.anim.overshoot_interpolator
			)

			resetInterpolator = getInterpolator(
				context,
				R.styleable.CanvasView_resetInterpolator,
				android.R.anim.accelerate_decelerate_interpolator
			)
		}.recycle()

		epsilonZoom = Defaults.EPSILON_ZOOM

		matrixAnimator = ValueAnimator.ofObject(MatrixEvaluator(), Matrix(), Matrix()).apply {
			val timeAnimationCanvas = Defaults.TIME_ANIMATION

			duration = timeAnimationCanvas.toLong()

			addUpdateListener {
				val matrix = it.animatedValue as Matrix

				canvasMatrix.set(matrix)

				invalidate()
			}
		}

		zoomRange = minZoom..maxZoom
	}

	protected val canvasMatrix by lazy {
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

	override fun onDraw(canvas: Canvas) {
		canvas.setMatrix(canvasMatrix)
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

	private fun animateMatrix(targetMatrix: Matrix, timeInterpolator: TimeInterpolator) {
		if (canvasMatrix == targetMatrix) return

		with(matrixAnimator) {
			if (isRunning) cancel()

			interpolator = timeInterpolator

			val initialMatrix = Matrix(canvasMatrix)

			setObjectValues(initialMatrix, targetMatrix)

			start()
		}
	}

	private inner class MoveDetector : GestureDetector.SimpleOnGestureListener() {
		override fun onScroll(
			e1: MotionEvent,
			e2: MotionEvent,
			distanceX: Float,
			distanceY: Float
		): Boolean {
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