package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

@Composable
fun PensumGraphCanvas(
	model: PensumScreenModel,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val density = LocalDensity.current
	val coroutineScope = rememberCoroutineScope()
	val scale = remember { Animatable(InitialCanvasZoom) }
	val offsetX = remember { Animatable(0f) }
	val offsetY = remember { Animatable(0f) }

	BoxWithConstraints(
		modifier = modifier
			.fillMaxSize()
			.clipToBounds()
			.background(ScreenBackground)
			.testTag(PensumUiTags.Canvas)
	) {
		val viewportSizePx = Size(
			width = with(density) { maxWidth.toPx() },
			height = with(density) { maxHeight.toPx() }
		)
		val canvasSizePx = Size(
			width = with(density) { model.canvas.width.dp.toPx() },
			height = with(density) { model.canvas.height.dp.toPx() }
		)
		val panMarginPx = with(density) { CanvasPanMargin.toPx() }
		val graphKey = "${model.selection.careerCode}-${model.selection.year}-${model.selection.modalityId}"
		var selectedNodeId by remember(graphKey) { mutableStateOf<String?>(null) }
		val selectedRequirementEdgeIds = remember(model.edges, selectedNodeId) {
			model.requirementEdgeIdsTo(selectedNodeId)
		}
		val selectedRequirementNodeIds = remember(model.edges, selectedNodeId, selectedRequirementEdgeIds) {
			model.requirementNodeIdsIn(
				selectedNodeId = selectedNodeId,
				selectedRequirementEdgeIds = selectedRequirementEdgeIds
			)
		}

		LaunchedEffect(graphKey, viewportSizePx, canvasSizePx, panMarginPx) {
			scale.snapTo(InitialCanvasZoom)
			val initialOffset = constrainCanvasOffset(
				offset = Offset(
					x = with(density) { InitialCanvasOffset.toPx() },
					y = with(density) { InitialCanvasOffset.toPx() }
				),
				scale = scale.value,
				canvasSizePx = canvasSizePx,
				viewportSizePx = viewportSizePx,
				panMarginPx = panMarginPx
			)
			offsetX.snapTo(initialOffset.x)
			offsetY.snapTo(initialOffset.y)
		}

		fun zoomTo(targetScale: Float) {
			coroutineScope.launch {
				val oldScale = scale.value
				val newScale = targetScale.coerceIn(MinCanvasZoom, MaxCanvasZoom)
				val viewportCenter = Offset(
					x = viewportSizePx.width / 2f,
					y = viewportSizePx.height / 2f
				)
				val currentOffset = Offset(offsetX.value, offsetY.value)
				val targetOffset = constrainCanvasOffset(
					offset = currentOffset.zoomedAround(
						anchor = viewportCenter,
						oldScale = oldScale,
						newScale = newScale
					),
					scale = newScale,
					canvasSizePx = canvasSizePx,
					viewportSizePx = viewportSizePx,
					panMarginPx = panMarginPx
				)

				scale.stop()
				offsetX.stop()
				offsetY.stop()
				launch {
					scale.animateTo(
						targetValue = newScale,
						animationSpec = tween(
							durationMillis = ZoomAnimationMillis,
							easing = DecelerateEasing
						)
					)
				}
				launch {
					offsetX.animateTo(
						targetValue = targetOffset.x,
						animationSpec = tween(
							durationMillis = ZoomAnimationMillis,
							easing = DecelerateEasing
						)
					)
				}
				launch {
					offsetY.animateTo(
						targetValue = targetOffset.y,
						animationSpec = tween(
							durationMillis = ZoomAnimationMillis,
							easing = DecelerateEasing
						)
					)
				}
			}
		}

		Box(
			modifier = Modifier
				.pointerInput(graphKey, viewportSizePx, canvasSizePx, panMarginPx) {
					detectTransformGestures { centroid, pan, zoom, _ ->
						val oldScale = scale.value
						val newScale = (oldScale * zoom).coerceIn(MinCanvasZoom, MaxCanvasZoom)
						val currentOffset = Offset(offsetX.value, offsetY.value)
						val nextOffset = constrainCanvasOffset(
							offset = currentOffset
								.zoomedAround(
									anchor = centroid,
									oldScale = oldScale,
									newScale = newScale
								) + pan,
							scale = newScale,
							canvasSizePx = canvasSizePx,
							viewportSizePx = viewportSizePx,
							panMarginPx = panMarginPx
						)

						coroutineScope.launch {
							scale.stop()
							offsetX.stop()
							offsetY.stop()
							scale.snapTo(newScale)
							offsetX.snapTo(nextOffset.x)
							offsetY.snapTo(nextOffset.y)
						}
					}
				}
				.graphicsLayer {
					translationX = offsetX.value
					translationY = offsetY.value
					scaleX = scale.value
					scaleY = scale.value
					transformOrigin = TransformOrigin(0f, 0f)
				}
				.size(model.canvas.width.dp, model.canvas.height.dp)
		) {
			Canvas(
				modifier = Modifier
					.fillMaxSize()
					.pointerInput(graphKey) {
						detectTapGestures(onTap = { selectedNodeId = null })
					}
			) {
				drawCanvasBackground(
					model = model,
					density = density.density,
					selectedRequirementEdgeIds = selectedRequirementEdgeIds
				)
			}
			model.terms.forEach { term ->
				Text(
					modifier = Modifier
						.offset(x = term.x.dp, y = 16.dp)
						.width(term.width.dp),
					text = term.label,
					textAlign = TextAlign.Center,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					color = TextSecondary
				)
			}
			model.nodes.forEach { node ->
				PensumNodeCard(
					node = node,
					isSelected = node.id == selectedNodeId,
					isRequirementHighlighted = node.id in selectedRequirementNodeIds,
					onSubjectStatsClick = onSubjectStatsClick,
					modifier = Modifier
						.offset(x = node.x.dp, y = node.y.dp)
						.size(width = node.width.dp, height = node.height.dp)
						.clickable {
							selectedNodeId = if (selectedNodeId == node.id) null else node.id
						}
						.testTag(PensumUiTags.node(node.id))
				)
			}
		}

		Row(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(end = 16.dp, bottom = 16.dp),
			horizontalArrangement = Arrangement.spacedBy(GraphControlsGap),
			verticalAlignment = Alignment.Bottom
		) {
			PensumMinimap(
				model = model,
				scale = scale.value,
				offset = Offset(offsetX.value, offsetY.value),
				viewportSizePx = viewportSizePx,
				selectedRequirementEdgeIds = selectedRequirementEdgeIds,
				densityScale = density.density
			)
			PensumZoomControls(
				onZoomIn = { zoomTo(scale.value + ZoomButtonStep) },
				onZoomOut = { zoomTo(scale.value - ZoomButtonStep) }
			)
		}
	}
}

private fun DrawScope.drawCanvasBackground(
	model: PensumScreenModel,
	density: Float,
	selectedRequirementEdgeIds: Set<String>
) {
	val widthPx = model.canvas.width.toFloat() * density
	val heightPx = model.canvas.height.toFloat() * density
	drawRoundRect(
		color = Color(0xFF141516),
		size = Size(widthPx, heightPx),
		cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
	)
	model.terms.forEach { term ->
		val x = term.x.toFloat() * density
		val termWidth = term.width.toFloat() * density
		drawRect(
			color = PanelBackground.copy(alpha = 0.42f),
			topLeft = Offset(x, 0f),
			size = Size(termWidth, heightPx)
		)
		drawLine(
			color = PanelBorder,
			start = Offset(x, 0f),
			end = Offset(x, heightPx),
			strokeWidth = 1.dp.toPx()
		)
	}
	drawRoundRect(
		color = PanelBorder,
		size = Size(widthPx, heightPx),
		cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
		style = Stroke(width = 1.dp.toPx())
	)
	model.edges.forEach { edge ->
		if (edge.id !in selectedRequirementEdgeIds) {
			drawPensumEdge(edge, model, density, isHighlighted = false)
		}
	}
	model.edges.forEach { edge ->
		if (edge.id in selectedRequirementEdgeIds) {
			drawPensumEdge(edge, model, density, isHighlighted = true)
		}
	}
}

private fun DrawScope.drawPensumEdge(
	edge: PensumScreenModel.Edge,
	model: PensumScreenModel,
	density: Float,
	isHighlighted: Boolean
) {
	if (edge.points.size < 2) return
	val color = if (isHighlighted) Selected else Available
	val strokeWidth = if (isHighlighted) 4.dp else 2.dp
	val points = model.edgeRoute(
		edge = edge,
		endpointGap = EdgeEndpointGap.toPx() / density,
		rerouteSpacing = EdgeRerouteSpacing.toPx() / density
	).map { point -> Offset(point.x * density, point.y * density) }
	if (points.size < 2) return
	drawPath(
		path = points.toRoundedOrthogonalPath(cornerRadius = EdgeCornerRadius.toPx()),
		color = color,
		style = Stroke(
			width = strokeWidth.toPx(),
			cap = StrokeCap.Round
		)
	)
	drawArrowHead(points[points.lastIndex - 1], points.last(), color)
}

private fun DrawScope.drawArrowHead(start: Offset, end: Offset, color: Color) {
	val angle = atan2(end.y - start.y, end.x - start.x)
	val arrowLength = ArrowHeadLength.toPx()
	val arrowAngle = PI.toFloat() / 7f
	val p1 = Offset(
		x = end.x - arrowLength * cos(angle - arrowAngle),
		y = end.y - arrowLength * sin(angle - arrowAngle)
	)
	val p2 = Offset(
		x = end.x - arrowLength * cos(angle + arrowAngle),
		y = end.y - arrowLength * sin(angle + arrowAngle)
	)
	val path = Path().apply {
		moveTo(end.x, end.y)
		lineTo(p1.x, p1.y)
		lineTo(p2.x, p2.y)
		close()
	}
	drawPath(path = path, color = color)
}

private fun Offset.zoomedAround(
	anchor: Offset,
	oldScale: Float,
	newScale: Float
): Offset {
	val scaleChange = newScale / oldScale
	return this + (anchor - this) * (1f - scaleChange)
}

private fun PensumScreenModel.requirementEdgeIdsTo(nodeId: String?): Set<String> {
	if (nodeId == null) return emptySet()

	val incomingRequirementEdges = edges
		.filter { edge -> edge.relationshipType == PensumScreenModel.RelationshipType.REQUIREMENT }
		.groupBy { edge -> edge.toNodeId }
	val selectedEdgeIds = mutableSetOf<String>()
	val visitedNodeIds = mutableSetOf<String>()

	fun collectRequirements(targetNodeId: String) {
		if (!visitedNodeIds.add(targetNodeId)) return

		incomingRequirementEdges[targetNodeId].orEmpty().forEach { edge ->
			selectedEdgeIds += edge.id
			collectRequirements(edge.fromNodeId)
		}
	}

	collectRequirements(nodeId)
	return selectedEdgeIds
}

private fun PensumScreenModel.requirementNodeIdsIn(
	selectedNodeId: String?,
	selectedRequirementEdgeIds: Set<String>
): Set<String> {
	if (selectedNodeId == null) return emptySet()

	return buildSet {
		add(selectedNodeId)
		edges.forEach { edge ->
			if (edge.id in selectedRequirementEdgeIds) {
				add(edge.fromNodeId)
				add(edge.toNodeId)
			}
		}
	}
}

private fun constrainCanvasOffset(
	offset: Offset,
	scale: Float,
	canvasSizePx: Size,
	viewportSizePx: Size,
	panMarginPx: Float
): Offset {
	return Offset(
		x = constrainCanvasAxisOffset(
			offset = offset.x,
			contentSizePx = canvasSizePx.width * scale,
			viewportSizePx = viewportSizePx.width,
			panMarginPx = panMarginPx
		),
		y = constrainCanvasAxisOffset(
			offset = offset.y,
			contentSizePx = canvasSizePx.height * scale,
			viewportSizePx = viewportSizePx.height,
			panMarginPx = panMarginPx
		)
	)
}

private fun constrainCanvasAxisOffset(
	offset: Float,
	contentSizePx: Float,
	viewportSizePx: Float,
	panMarginPx: Float
): Float {
	if (contentSizePx <= viewportSizePx) {
		val centeredOffset = (viewportSizePx - contentSizePx) / 2f
		return offset.coerceIn(centeredOffset - panMarginPx, centeredOffset + panMarginPx)
	}

	return offset.coerceIn(
		minimumValue = viewportSizePx - contentSizePx - panMarginPx,
		maximumValue = panMarginPx
	)
}
