package com.gdavidpb.tuindice.ui.customs.graphs.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.ColorInt
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.inset
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.resolveColor
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.resolveDimension
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.transform
import com.gdavidpb.tuindice.ui.customs.graphs.models.Node
import com.gdavidpb.tuindice.utils.extensions.getFloat
import com.gdavidpb.tuindice.utils.extensions.loadAttributes
import com.gdavidpb.tuindice.utils.extensions.supportQuickReject

open class NetworkView(context: Context, attrs: AttributeSet) : CanvasView(context, attrs) {

    private val nodes: MutableMap<Node, Boolean> = hashMapOf()

    @ColorInt
    private val textColor: Int

    @ColorInt
    private val nodeColor: Int

    @ColorInt
    private val connectionColor: Int

    private val tapZoom: Float

    private val nodeRadius: Float
    private val nodeTextSize: Float
    private val nodeTextRelation: Float
    private val connectionWidth: Float
    private val relationTouch: Float

    private val nodePaint: Paint
    private val textPaint: Paint
    private val connectionPaint: Paint

    init {
        loadAttributes(R.styleable.NetworkView, attrs).apply {
            textColor = resolveColor(context,
                    R.styleable.NetworkView_textColor,
                    R.color.color_node_text
            )

            nodeColor = resolveColor(context,
                    R.styleable.NetworkView_nodeColor,
                    R.color.color_node
            )

            connectionColor = resolveColor(context,
                    R.styleable.NetworkView_connectionColor,
                    R.color.color_node_connection
            )

            nodeRadius = resolveDimension(context,
                    R.styleable.NetworkView_nodeRadius,
                    R.dimen.size_node_radius
            )

            connectionWidth = resolveDimension(context,
                    R.styleable.NetworkView_connectionWidth,
                    R.dimen.size_node_connection_width
            )

        }.recycle()

        nodeTextRelation = context.getFloat(R.dimen.relation_node_text)

        nodeTextSize = nodeRadius / nodeTextRelation

        relationTouch = context.getFloat(R.dimen.relation_node_touch)

        tapZoom = context.getFloat(R.dimen.zoom_tap)

        nodePaint = Paint().apply {
            color = nodeColor

            isAntiAlias = true
        }

        textPaint = Paint().apply {
            color = textColor
            textSize = nodeTextSize

            isAntiAlias = true
        }

        connectionPaint = Paint().apply {
            color = connectionColor
            strokeWidth = connectionWidth

            isAntiAlias = true
        }
    }

    private val moveDetector by lazy {
        GestureDetector(context, MoveDetector())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val isCustomHandle = moveDetector.onTouchEvent(event)

        if (isCustomHandle) invalidate()

        return isCustomHandle || super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        /* Draw, compute, cache nodes */
        for ((node, _) in nodes) {
            val isVisible = !canvas.supportQuickReject(node.path)

            nodes[node] = isVisible

            if (isVisible) canvas.drawPath(node.path, node.paint)
        }
    }

    override fun onZoom(factor: Float) {
        // TODO
    }

    override fun onMove(x: Float, y: Float) {
        // TODO
    }

    fun addNodes(vararg values: Node) {
        nodes.putAll(values.associate { it to false })
        invalidate()
    }

    fun clear() {
        nodes.clear()
        invalidate()
    }

    private inner class MoveDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val cantHandle = e.pointerCount > 1

            if (cantHandle) return false

            val bounds = RectF()
            val foundNode = getNodeByPosition(e.x, e.y, bounds)

            return if (foundNode != null) {
                val canTap = foundNode.isClickable

                if (canTap) {
                    // TODO onTap(foundNode, e.x, e.y)
                }

                canTap
            } else {
                false
            }
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val cantHandle = e.pointerCount > 1

            if (cantHandle) return false

            val bounds = RectF()
            val foundNode = getNodeByPosition(e.x, e.y, bounds)

            return if (foundNode != null) {
                val canTap = foundNode.isClickable

                if (canTap)
                    zoomTo(factor = tapZoom, x = bounds.centerX(), y = bounds.centerY())

                canTap
            } else {
                moveTo(x = e.x, y = e.y)

                true
            }
        }
    }

    private fun getNodeByPosition(x: Float, y: Float, bounds: RectF): Node? {
        return nodes.entries.find { (node, isVisible) ->
            if (isVisible) {
                /* Compute node bounds */
                node.path.computeBounds(bounds, true)

                /* Apply canvas matrix */
                bounds.apply {
                    transform(canvasMatrix)
                    inset(relationTouch)
                }.contains(x, y)
            } else {
                false
            }
        }?.key
    }
}