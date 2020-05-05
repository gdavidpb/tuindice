package com.gdavidpb.tuindice.ui.customs.graphs.views

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.resolveColor
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.resolveDimension
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.resolveFloat
import com.gdavidpb.tuindice.ui.customs.graphs.models.CanvasObject
import com.gdavidpb.tuindice.ui.customs.graphs.models.Node
import com.gdavidpb.tuindice.utils.extensions.loadAttributes

class NetworkView(context: Context, attrs: AttributeSet) : CanvasView(context, attrs) {

    private val nodes: MutableList<Node> = mutableListOf()

    @ColorInt
    private val textColor: Int

    @ColorInt
    private val nodeColor: Int

    @ColorInt
    private val connectionColor: Int

    private val nodeRadius: Float
    private val nodeTextSize: Float
    private val nodeTextRelation: Float
    private val connectionWidth: Float

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

        nodeTextRelation = resolveFloat(context, R.dimen.relation_node_text)

        nodeTextSize = nodeRadius / nodeTextRelation

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

    override fun onZoom(factor: Float) {
    }

    override fun onMove(x: Float, y: Float) {
    }

    override fun onTap(canvasObject: CanvasObject, x: Float, y: Float) {
    }

    override fun clear() {
        super.clear()

        nodes.clear()
    }

    fun addNodes(vararg values: Node) {
        val objects = values.map { node ->
            val nodePath = Path()
            val nodePaint = Paint()

            CanvasObject(nodePath, nodePaint, true)
        }.toTypedArray()

        addObjects(*objects)

        nodes.addAll(values)
    }
}