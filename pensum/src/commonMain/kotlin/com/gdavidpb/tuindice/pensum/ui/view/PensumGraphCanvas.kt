package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PensumGraphCanvas(
	model: PensumScreenModel,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val density = LocalDensity.current
	val coroutineScope = rememberCoroutineScope()
	val graphKey = "${model.selection.year}-${model.selection.modalityId}"
	var savedScale by rememberSaveable(graphKey) { mutableStateOf<Float?>(null) }
	var savedOffsetX by rememberSaveable(graphKey) { mutableStateOf<Float?>(null) }
	var savedOffsetY by rememberSaveable(graphKey) { mutableStateOf<Float?>(null) }
	var isMinimapToggleVisible by rememberSaveable(graphKey) { mutableStateOf(false) }
	var isMinimapVisible by rememberSaveable(graphKey) { mutableStateOf(false) }
	val scale = remember(graphKey) { Animatable(savedScale ?: InitialCanvasZoom) }
	val offsetX = remember(graphKey) { Animatable(savedOffsetX ?: 0f) }
	val offsetY = remember(graphKey) { Animatable(savedOffsetY ?: 0f) }
	var canvasSnapJob by remember(graphKey) { mutableStateOf<Job?>(null) }

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
		val fitPaddingPx = with(density) { CanvasFitPadding.toPx() }
		val focusPaddingPx = with(density) { CanvasFocusPadding.toPx() }
		val snapDistancePx = with(density) { CanvasSnapDistance.toPx() }
		val snapInsetPx = with(density) { CanvasSnapViewportInset.toPx() }
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
		val selectedUnlockEdgeIds = remember(model.edges, selectedNodeId) {
			model.unlockEdgeIdsFrom(selectedNodeId)
		}
		val selectedUnlockNodeIds = remember(model.edges, selectedNodeId, selectedUnlockEdgeIds) {
			model.unlockNodeIdsIn(
				selectedNodeId = selectedNodeId,
				selectedUnlockEdgeIds = selectedUnlockEdgeIds
			)
		}
		val selectedFocusNodeIds = remember(selectedRequirementNodeIds, selectedUnlockNodeIds) {
			selectedRequirementNodeIds + selectedUnlockNodeIds
		}

		fun saveCanvasViewport(scaleValue: Float, offset: Offset) {
			savedScale = scaleValue
			savedOffsetX = offset.x
			savedOffsetY = offset.y
		}

		LaunchedEffect(graphKey, viewportSizePx, canvasSizePx, panMarginPx) {
			val restoredScale = savedScale?.coerceIn(
				minimumValue = MinCanvasZoom,
				maximumValue = MaxCanvasZoom
			) ?: InitialCanvasZoom
			val restoredOffset = if (savedOffsetX != null && savedOffsetY != null) {
				Offset(
					x = checkNotNull(savedOffsetX),
					y = checkNotNull(savedOffsetY)
				)
			} else {
				Offset(
					x = with(density) { InitialCanvasOffset.toPx() },
					y = with(density) { InitialCanvasOffset.toPx() }
				)
			}
			val constrainedOffset = constrainCanvasOffset(
				offset = restoredOffset,
				scale = restoredScale,
				canvasSizePx = canvasSizePx,
				viewportSizePx = viewportSizePx,
				panMarginPx = panMarginPx
			)
			scale.snapTo(restoredScale)
			offsetX.snapTo(constrainedOffset.x)
			offsetY.snapTo(constrainedOffset.y)
			saveCanvasViewport(
				scaleValue = restoredScale,
				offset = constrainedOffset
			)
		}

		fun animateCanvasViewport(targetScale: Float, targetOffset: Offset) {
			coroutineScope.launch {
				scale.stop()
				offsetX.stop()
				offsetY.stop()
				launch {
					scale.animateTo(
						targetValue = targetScale,
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
				saveCanvasViewport(
					scaleValue = targetScale,
					offset = targetOffset
				)
			}
		}

		fun snapCanvasViewport(targetScale: Float, targetOffset: Offset) {
			canvasSnapJob?.cancel()
			coroutineScope.launch {
				scale.stop()
				offsetX.stop()
				offsetY.stop()
				scale.snapTo(targetScale)
				offsetX.snapTo(targetOffset.x)
				offsetY.snapTo(targetOffset.y)
				saveCanvasViewport(
					scaleValue = targetScale,
					offset = targetOffset
				)
			}
		}

		fun revealMinimapToggle() {
			isMinimapToggleVisible = true
		}

		fun hideMinimap() {
			isMinimapToggleVisible = false
			isMinimapVisible = false
		}

		fun viewportOffsetForCanvasCenter(
			canvasCenter: Offset,
			targetScale: Float
		): Offset {
			val viewportAnchor = Offset(
				x = viewportSizePx.width / 2f,
				y = viewportSizePx.height * 0.46f
			)
			return constrainCanvasOffset(
				offset = Offset(
					x = viewportAnchor.x - canvasCenter.x * density.density * targetScale,
					y = viewportAnchor.y - canvasCenter.y * density.density * targetScale
				),
				scale = targetScale,
				canvasSizePx = canvasSizePx,
				viewportSizePx = viewportSizePx,
				panMarginPx = panMarginPx
			)
		}

		fun centerCanvasBounds(
			bounds: CanvasBounds,
			targetScale: Float
		) {
			canvasSnapJob?.cancel()
			revealMinimapToggle()
			animateCanvasViewport(
				targetScale = targetScale,
				targetOffset = viewportOffsetForCanvasCenter(
					canvasCenter = bounds.center,
					targetScale = targetScale
				)
			)
		}

		fun centerSelectedNode(node: PensumScreenModel.Node) {
			centerCanvasBounds(
				bounds = node.canvasBounds(),
				targetScale = max(scale.value, NodeFocusMinZoom).coerceIn(
					minimumValue = MinCanvasZoom,
					maximumValue = MaxCanvasZoom
				)
			)
		}

		fun focusProgress() {
			val bounds = model.progressFocusBounds() ?: return
			centerCanvasBounds(
				bounds = bounds,
				targetScale = focusCanvasScale(
					bounds = bounds,
					viewportSizePx = viewportSizePx,
					densityScale = density.density,
					paddingPx = focusPaddingPx,
					maxScale = ProgressFocusMaxZoom
				)
			)
		}

		fun moveViewportToCanvasCenter(canvasCenter: Offset) {
			revealMinimapToggle()
			val targetOffset = viewportOffsetForCanvasCenter(
				canvasCenter = canvasCenter,
				targetScale = scale.value
			)
			snapCanvasViewport(
				targetScale = scale.value,
				targetOffset = targetOffset
			)
		}

		fun scheduleViewportSnap(scaleValue: Float, offset: Offset) {
			canvasSnapJob?.cancel()
			canvasSnapJob = coroutineScope.launch {
				delay(CanvasSnapDelayMillis.toLong())
				val snappedOffset = snapCanvasOffset(
					model = model,
					offset = offset,
					scale = scaleValue,
					canvasSizePx = canvasSizePx,
					viewportSizePx = viewportSizePx,
					densityScale = density.density,
					panMarginPx = panMarginPx,
					snapDistancePx = snapDistancePx,
					snapInsetPx = snapInsetPx
				)
				canvasSnapJob = null
				if (snappedOffset != offset) {
					animateCanvasViewport(
						targetScale = scaleValue,
						targetOffset = snappedOffset
					)
				}
			}
		}

		fun zoomTo(targetScale: Float, shouldRevealMinimapToggle: Boolean = true) {
			canvasSnapJob?.cancel()
			if (shouldRevealMinimapToggle) {
				revealMinimapToggle()
			}
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

			animateCanvasViewport(
				targetScale = newScale,
				targetOffset = targetOffset
			)
		}

		fun fitCanvasOffset(targetScale: Float): Offset {
			return constrainCanvasOffset(
				offset = centerCanvasOffset(
					scale = targetScale,
					canvasSizePx = canvasSizePx,
					viewportSizePx = viewportSizePx
				),
				scale = targetScale,
				canvasSizePx = canvasSizePx,
				viewportSizePx = viewportSizePx,
				panMarginPx = panMarginPx
			)
		}

		fun fitToScreen() {
			canvasSnapJob?.cancel()
			hideMinimap()
			val targetScale = fitCanvasScale(
				viewportSizePx = viewportSizePx,
				canvasSizePx = canvasSizePx,
				paddingPx = fitPaddingPx
			)

			animateCanvasViewport(
				targetScale = targetScale,
				targetOffset = fitCanvasOffset(targetScale)
			)
		}

		fun zoomOut() {
			val fitScale = fitCanvasScale(
				viewportSizePx = viewportSizePx,
				canvasSizePx = canvasSizePx,
				paddingPx = fitPaddingPx
			)
			if (scale.value - ZoomButtonStep <= fitScale) {
				fitToScreen()
			} else {
				zoomTo(scale.value - ZoomButtonStep)
			}
		}

		fun toggleDoubleTapZoom(anchor: Offset) {
			if (scale.value >= DoubleTapZoomOutThreshold) {
				fitToScreen()
				return
			}

			canvasSnapJob?.cancel()
			val targetScale = DoubleTapCanvasZoom.coerceIn(MinCanvasZoom, MaxCanvasZoom)
			revealMinimapToggle()
			val targetOffset = constrainCanvasOffset(
				offset = Offset(offsetX.value, offsetY.value).zoomedAround(
					anchor = anchor,
					oldScale = scale.value,
					newScale = targetScale
				),
				scale = targetScale,
				canvasSizePx = canvasSizePx,
				viewportSizePx = viewportSizePx,
				panMarginPx = panMarginPx
			)

			animateCanvasViewport(
				targetScale = targetScale,
				targetOffset = targetOffset
			)
		}

		Box(
			modifier = Modifier
				.pointerInput(graphKey, viewportSizePx, canvasSizePx, panMarginPx) {
					detectTransformGestures { centroid, pan, zoom, _ ->
						revealMinimapToggle()
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
							saveCanvasViewport(
								scaleValue = newScale,
								offset = nextOffset
							)
							scheduleViewportSnap(
								scaleValue = newScale,
								offset = nextOffset
							)
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
						detectTapGestures(
							onTap = { selectedNodeId = null },
							onDoubleTap = { tapOffset -> toggleDoubleTapZoom(tapOffset) }
						)
					}
			) {
				drawCanvasBackground(
					model = model,
					density = density.density,
					selectedRequirementEdgeIds = selectedRequirementEdgeIds,
					selectedUnlockEdgeIds = selectedUnlockEdgeIds,
					isFocusActive = selectedNodeId != null
				)
			}
			model.nodes.forEach { node ->
				val isUnlockHighlighted = node.id in selectedUnlockNodeIds
				val isNodeDimmed = selectedNodeId != null && node.id !in selectedFocusNodeIds
				PensumNodeCard(
					node = node,
					isSelected = node.id == selectedNodeId,
					isRequirementHighlighted = node.id in selectedRequirementNodeIds,
					isUnlockHighlighted = isUnlockHighlighted,
					onSubjectStatsClick = onSubjectStatsClick,
					modifier = Modifier
						.offset(x = node.x.dp, y = node.y.dp)
						.size(width = node.width.dp, height = node.height.dp)
						.graphicsLayer {
							alpha = if (isNodeDimmed) 0.34f else 1f
						}
						.clickable {
							val nextSelectedNodeId = if (selectedNodeId == node.id) null else node.id
							selectedNodeId = nextSelectedNodeId
							if (nextSelectedNodeId != null) {
								centerSelectedNode(node)
							}
						}
						.testTag(PensumUiTags.node(node.id))
				)
			}
		}

		PensumStickyTermHeader(
			terms = model.terms,
			scale = scale.value,
			offsetX = offsetX.value,
			densityScale = density.density,
			modifier = Modifier.align(Alignment.TopStart)
		)

		if (isMinimapToggleVisible && isMinimapVisible) {
			PensumMinimap(
				model = model,
				scale = scale.value,
				offset = Offset(offsetX.value, offsetY.value),
				viewportSizePx = viewportSizePx,
				selectedRequirementEdgeIds = selectedRequirementEdgeIds,
				selectedUnlockEdgeIds = selectedUnlockEdgeIds,
				densityScale = density.density,
				onViewportCenterChange = { canvasCenter -> moveViewportToCanvasCenter(canvasCenter) },
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(start = 16.dp, bottom = CanvasBottomOverlayPadding)
			)
		}

		PensumZoomControls(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(end = 16.dp, bottom = CanvasBottomOverlayPadding),
			isMinimapToggleVisible = isMinimapToggleVisible,
			isMinimapVisible = isMinimapVisible,
			onFocusProgress = { focusProgress() },
			onFitToScreen = { fitToScreen() },
			onToggleMinimap = { isMinimapVisible = !isMinimapVisible },
			onZoomIn = { zoomTo(scale.value + ZoomButtonStep) },
			onZoomOut = { zoomOut() }
		)

		PensumCanvasLegend(
			modifier = Modifier.align(Alignment.BottomCenter)
		)
	}
}

private fun DrawScope.drawCanvasBackground(
	model: PensumScreenModel,
	density: Float,
	selectedRequirementEdgeIds: Set<String>,
	selectedUnlockEdgeIds: Set<String>,
	isFocusActive: Boolean
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
	val focusedEdgeIds = selectedRequirementEdgeIds + selectedUnlockEdgeIds
	model.edges.forEach { edge ->
		if (edge.id !in focusedEdgeIds) {
			drawPensumEdge(
				edge = edge,
				model = model,
				density = density,
				focusTone = if (isFocusActive) EdgeFocusTone.Dimmed else EdgeFocusTone.Default
			)
		}
	}
	model.edges.forEach { edge ->
		if (edge.id in selectedRequirementEdgeIds) {
			drawPensumEdge(
				edge = edge,
				model = model,
				density = density,
				focusTone = EdgeFocusTone.Requirement
			)
		}
	}
	model.edges.forEach { edge ->
		if (edge.id in selectedUnlockEdgeIds && edge.id !in selectedRequirementEdgeIds) {
			drawPensumEdge(
				edge = edge,
				model = model,
				density = density,
				focusTone = EdgeFocusTone.Unlock
			)
		}
	}
}

private fun DrawScope.drawPensumEdge(
	edge: PensumScreenModel.Edge,
	model: PensumScreenModel,
	density: Float,
	focusTone: EdgeFocusTone
) {
	if (edge.points.size < 2) return
	val color = when (focusTone) {
		EdgeFocusTone.Default -> Available
		EdgeFocusTone.Dimmed -> Available.copy(alpha = 0.18f)
		EdgeFocusTone.Requirement -> Selected
		EdgeFocusTone.Unlock -> Current
	}
	val strokeWidth = when (focusTone) {
		EdgeFocusTone.Default -> 2.dp
		EdgeFocusTone.Dimmed -> 1.4.dp
		EdgeFocusTone.Requirement,
		EdgeFocusTone.Unlock -> 4.dp
	}
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
			cap = StrokeCap.Round,
			pathEffect = pathEffect
		)
	)
	drawArrowHead(points[points.lastIndex - 1], points.last(), color)
}

private enum class EdgeFocusTone {
	Default,
	Dimmed,
	Requirement,
	Unlock
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

private fun fitCanvasScale(
	viewportSizePx: Size,
	canvasSizePx: Size,
	paddingPx: Float
): Float {
	if (canvasSizePx.width <= 0f || canvasSizePx.height <= 0f) return InitialCanvasZoom

	val availableWidth = (viewportSizePx.width - paddingPx * 2f).coerceAtLeast(1f)
	val availableHeight = (viewportSizePx.height - paddingPx * 2f).coerceAtLeast(1f)
	return min(
		availableWidth / canvasSizePx.width,
		availableHeight / canvasSizePx.height
	).coerceIn(
		minimumValue = MinCanvasFitZoom,
		maximumValue = MaxCanvasZoom
	)
}

private fun centerCanvasOffset(
	scale: Float,
	canvasSizePx: Size,
	viewportSizePx: Size
): Offset {
	return Offset(
		x = (viewportSizePx.width - canvasSizePx.width * scale) / 2f,
		y = (viewportSizePx.height - canvasSizePx.height * scale) / 2f
	)
}

private data class CanvasBounds(
	val left: Float,
	val top: Float,
	val right: Float,
	val bottom: Float
) {
	val width: Float get() = (right - left).coerceAtLeast(1f)
	val height: Float get() = (bottom - top).coerceAtLeast(1f)
	val center: Offset get() = Offset(x = left + width / 2f, y = top + height / 2f)
}

private fun PensumScreenModel.Node.canvasBounds(): CanvasBounds {
	return CanvasBounds(
		left = x.toFloat(),
		top = y.toFloat(),
		right = (x + width).toFloat(),
		bottom = (y + height).toFloat()
	)
}

private fun List<PensumScreenModel.Node>.canvasBounds(): CanvasBounds? {
	if (isEmpty()) return null
	return CanvasBounds(
		left = minOf { node -> node.x.toFloat() },
		top = minOf { node -> node.y.toFloat() },
		right = maxOf { node -> (node.x + node.width).toFloat() },
		bottom = maxOf { node -> (node.y + node.height).toFloat() }
	)
}

private fun PensumScreenModel.progressFocusBounds(): CanvasBounds? {
	val currentNodes = nodes.filter { node -> node.isCurrent }
	if (currentNodes.isNotEmpty()) return currentNodes.canvasBounds()

	val availableNodes = nodes.filter { node -> !node.isApproved && !node.isBlocked }
	if (availableNodes.isNotEmpty()) return availableNodes.leadingColumn().canvasBounds()

	val approvedNodes = nodes.filter { node -> node.isApproved }
	if (approvedNodes.isNotEmpty()) return approvedNodes.trailingColumn().canvasBounds()

	return nodes.firstOrNull()?.canvasBounds()
}

private fun List<PensumScreenModel.Node>.leadingColumn(): List<PensumScreenModel.Node> {
	val firstX = minOf { node -> node.x }
	return filter { node -> abs(node.x - firstX) < 1.0 }
}

private fun List<PensumScreenModel.Node>.trailingColumn(): List<PensumScreenModel.Node> {
	val lastX = maxOf { node -> node.x }
	return filter { node -> abs(node.x - lastX) < 1.0 }
}

private fun focusCanvasScale(
	bounds: CanvasBounds,
	viewportSizePx: Size,
	densityScale: Float,
	paddingPx: Float,
	maxScale: Float
): Float {
	val availableWidth = (viewportSizePx.width - paddingPx * 2f).coerceAtLeast(1f)
	val availableHeight = (viewportSizePx.height - paddingPx * 2f).coerceAtLeast(1f)
	val boundsWidthPx = bounds.width * densityScale
	val boundsHeightPx = bounds.height * densityScale
	return min(
		availableWidth / boundsWidthPx,
		availableHeight / boundsHeightPx
	).coerceIn(
		minimumValue = MinCanvasZoom,
		maximumValue = maxScale
	)
}

private fun snapCanvasOffset(
	model: PensumScreenModel,
	offset: Offset,
	scale: Float,
	canvasSizePx: Size,
	viewportSizePx: Size,
	densityScale: Float,
	panMarginPx: Float,
	snapDistancePx: Float,
	snapInsetPx: Float
): Offset {
	val nearestTermOffsetX = model.terms
		.map { term -> snapInsetPx - term.x.toFloat() * densityScale * scale }
		.minByOrNull { targetX -> abs(targetX - offset.x) }
	val snappedX = if (nearestTermOffsetX != null && abs(nearestTermOffsetX - offset.x) <= snapDistancePx) {
		nearestTermOffsetX
	} else {
		offset.x
	}
	val snappedY = if (abs(offset.y - snapInsetPx) <= snapDistancePx) {
		snapInsetPx
	} else {
		offset.y
	}

	return constrainCanvasOffset(
		offset = Offset(snappedX, snappedY),
		scale = scale,
		canvasSizePx = canvasSizePx,
		viewportSizePx = viewportSizePx,
		panMarginPx = panMarginPx
	)
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

private fun PensumScreenModel.unlockEdgeIdsFrom(nodeId: String?): Set<String> {
	if (nodeId == null) return emptySet()

	val outgoingRequirementEdges = edges
		.filter { edge -> edge.relationshipType == PensumScreenModel.RelationshipType.REQUIREMENT }
		.groupBy { edge -> edge.fromNodeId }
	val selectedEdgeIds = mutableSetOf<String>()
	val visitedNodeIds = mutableSetOf<String>()

	fun collectUnlocks(sourceNodeId: String) {
		if (!visitedNodeIds.add(sourceNodeId)) return

		outgoingRequirementEdges[sourceNodeId].orEmpty().forEach { edge ->
			selectedEdgeIds += edge.id
			collectUnlocks(edge.toNodeId)
		}
	}

	collectUnlocks(nodeId)
	return selectedEdgeIds
}

private fun PensumScreenModel.unlockNodeIdsIn(
	selectedNodeId: String?,
	selectedUnlockEdgeIds: Set<String>
): Set<String> {
	if (selectedNodeId == null) return emptySet()

	return buildSet {
		add(selectedNodeId)
		edges.forEach { edge ->
			if (edge.id in selectedUnlockEdgeIds) {
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
