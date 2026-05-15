package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags

@Composable
fun PensumMinimap(
	model: PensumScreenModel,
	scale: Float,
	offset: Offset,
	viewportSizePx: Size,
	selectedRequirementEdgeIds: Set<String>,
	densityScale: Float,
	modifier: Modifier = Modifier
) {
	Canvas(
		modifier = modifier
			.size(MinimapWidth, MinimapHeight)
			.background(Color.Black.copy(alpha = 0.62f), RoundedCornerShape(8.dp))
			.border(1.dp, Available, RoundedCornerShape(8.dp))
			.padding(8.dp)
			.testTag(PensumUiTags.Minimap)
	) {
		val sx = size.width / model.canvas.width.toFloat()
		val sy = size.height / model.canvas.height.toFloat()
		model.edges.forEach { edge ->
			val color = if (edge.id in selectedRequirementEdgeIds) Selected.copy(alpha = 0.9f) else Available.copy(alpha = 0.55f)
			val strokeWidth = if (edge.id in selectedRequirementEdgeIds) 4f else 2f
			model.edgeRoute(
				edge = edge,
				endpointGap = EdgeEndpointGap.value,
				rerouteSpacing = EdgeRerouteSpacing.value
			).zipWithNext().forEach { (start, end) ->
				drawLine(
					color = color,
					start = Offset(start.x * sx, start.y * sy),
					end = Offset(end.x * sx, end.y * sy),
					strokeWidth = strokeWidth
				)
			}
		}
		model.nodes.forEach { node ->
			drawRoundRect(
				color = node.visualStyle.toNodeColors().border,
				topLeft = Offset(node.x.toFloat() * sx, node.y.toFloat() * sy),
				size = Size((node.width.toFloat() * sx).coerceAtLeast(5f), (node.height.toFloat() * sy).coerceAtLeast(4f)),
				cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
			)
		}

		val visibleCanvasWidth = (viewportSizePx.width / (densityScale * scale)).coerceAtMost(model.canvas.width.toFloat())
		val visibleCanvasHeight = (viewportSizePx.height / (densityScale * scale)).coerceAtMost(model.canvas.height.toFloat())
		val visibleCanvasX = (-offset.x / (densityScale * scale))
			.coerceIn(0f, (model.canvas.width.toFloat() - visibleCanvasWidth).coerceAtLeast(0f))
		val visibleCanvasY = (-offset.y / (densityScale * scale))
			.coerceIn(0f, (model.canvas.height.toFloat() - visibleCanvasHeight).coerceAtLeast(0f))

		drawRect(
			color = Current,
			topLeft = Offset(
				x = visibleCanvasX * sx,
				y = visibleCanvasY * sy
			),
			size = Size(
				width = visibleCanvasWidth * sx,
				height = visibleCanvasHeight * sy
			),
			style = Stroke(width = 1.dp.toPx())
		)
	}
}
