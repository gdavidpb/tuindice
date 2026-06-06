package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import kotlin.math.min

@Composable
fun PensumMinimap(
	model: PensumScreenModel,
	scale: Float,
	offset: Offset,
	viewportSizePx: Size,
	selectedRequirementEdgeIds: Set<String>,
	selectedUnlockEdgeIds: Set<String>,
	selectedAvailableUnlockEdgeIds: Set<String>,
	focusedNodeIds: Set<String>,
	isFocusActive: Boolean,
	densityScale: Float,
	onViewportCenterChange: (Offset) -> Unit,
	modifier: Modifier = Modifier
) {
	fun Offset.toCanvasCenter(minimapSize: androidx.compose.ui.unit.IntSize): Offset {
		if (minimapSize.width <= 0 || minimapSize.height <= 0) return Offset.Zero
		val sx = minimapSize.width.toFloat() / model.canvas.width.toFloat()
		val sy = minimapSize.height.toFloat() / model.canvas.height.toFloat()
		return Offset(
			x = (x / sx).coerceIn(0f, model.canvas.width.toFloat()),
			y = (y / sy).coerceIn(0f, model.canvas.height.toFloat())
		)
	}

	Canvas(
		modifier = modifier
			.size(MinimapWidth, MinimapHeight)
			.background(Color.Black.copy(alpha = 0.62f), PensumElementShape)
			.border(1.dp, CanvasNeutral, PensumElementShape)
			.padding(8.dp)
			.pointerInput(model.canvas, scale) {
				detectTapGestures(
					onTap = { position ->
						onViewportCenterChange(position.toCanvasCenter(size))
					}
				)
			}
			.pointerInput(model.canvas, scale) {
				detectDragGestures(
					onDragStart = { position ->
						onViewportCenterChange(position.toCanvasCenter(size))
					},
					onDrag = { change, _ ->
						onViewportCenterChange(change.position.toCanvasCenter(size))
						change.consume()
					}
				)
			}
			.testTag(PensumUiTags.Minimap)
	) {
		val sx = size.width / model.canvas.width.toFloat()
		val sy = size.height / model.canvas.height.toFloat()
		val nodeCornerRadius = PensumElementCornerRadius.value * min(sx, sy)
		model.edges.forEach { edge ->
			val isFocusedEdge = edge.id in selectedRequirementEdgeIds || edge.id in selectedUnlockEdgeIds
			val color = when {
				edge.id in selectedAvailableUnlockEdgeIds -> Available.copy(alpha = MinimapFocusedAlpha)
				isFocusedEdge -> Selected.copy(alpha = MinimapFocusedAlpha)
				else -> CanvasNeutral.copy(
					alpha = if (isFocusActive) MinimapDimmedAlpha else MinimapNeutralAlpha
				)
			}
			val strokeWidth = if (isFocusedEdge) 4f else 2f
			val pathEffect = if (edge.isDisconnected) {
				PathEffect.dashPathEffect(
					floatArrayOf(
						DisconnectedEdgeDashLength.toPx(),
						DisconnectedEdgeDashGap.toPx()
					)
				)
			} else {
				null
			}
			model.edgeRoute(
				edge = edge,
				endpointGap = EdgeEndpointGap.value,
				rerouteSpacing = EdgeRerouteSpacing.value
			).zipWithNext().forEach { (start, end) ->
				drawLine(
					color = color,
					start = Offset(start.x * sx, start.y * sy),
					end = Offset(end.x * sx, end.y * sy),
					strokeWidth = strokeWidth,
					pathEffect = pathEffect
				)
			}
		}
		model.nodes.forEach { node ->
			val nodeAlpha = if (isFocusActive && node.id !in focusedNodeIds) {
				MinimapDimmedAlpha
			} else {
				MinimapFocusedAlpha
			}
			drawRoundRect(
				color = node.visualStyle.toNodeColors().border.copy(alpha = nodeAlpha),
				topLeft = Offset(node.x.toFloat() * sx, node.y.toFloat() * sy),
				size = Size((node.width.toFloat() * sx).coerceAtLeast(5f), (node.height.toFloat() * sy).coerceAtLeast(4f)),
				cornerRadius = androidx.compose.ui.geometry.CornerRadius(nodeCornerRadius, nodeCornerRadius)
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

private const val MinimapFocusedAlpha = 0.9f
private const val MinimapNeutralAlpha = 0.55f
private const val MinimapDimmedAlpha = 0.18f
