package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.PensumGraphColors
import com.gdavidpb.tuindice.pensum.ui.model.focusStateFor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

internal val LocalPensumManualCanvasGestureActiveOverride = compositionLocalOf<Boolean?> { null }

@Composable
fun PensumGraphCanvas(
	model: PensumScreenModel,
	selectedNodeId: String?,
	onSelectedNodeChange: (String?) -> Unit,
	onFocusedNodeClick: (String) -> Unit = { onSelectedNodeChange(null) },
	isSubjectSheetVisible: Boolean = false,
	focusRequestSerial: Int = 0,
	modifier: Modifier = Modifier,
	sessionState: PensumScreenSessionStore.SelectionState? = null
) {
	val graphColors = pensumGraphColors()
	val density = LocalDensity.current
	val densityScale = density.density
	val coroutineScope = rememberCoroutineScope()
	val graphKey = "${model.selection.year}-${model.selection.modalityId}"
	val activeSessionState = sessionState ?: remember(graphKey) { PensumScreenSessionStore.SelectionState() }
	val edgeRouteEndpointGap = with(density) { EdgeEndpointGap.toPx() } / densityScale
	val edgeRouteRerouteSpacing = with(density) { EdgeRerouteSpacing.toPx() } / densityScale
	val edgeRoutes = remember(
		graphKey,
		model.edges,
		model.nodes,
		edgeRouteEndpointGap,
		edgeRouteRerouteSpacing
	) {
		model.edgeRoutes(
			endpointGap = edgeRouteEndpointGap,
			rerouteSpacing = edgeRouteRerouteSpacing
		)
	}
	val edgeCornerRadiusPx = with(density) { EdgeCornerRadius.toPx() }
	val arrowHeadLengthPx = with(density) { ArrowHeadLength.toPx() }
	// Each edge's line and arrowhead are built once per route, not on every draw of the edges.
	val edgeShapes = remember(graphKey, edgeRoutes, densityScale, edgeCornerRadiusPx, arrowHeadLengthPx) {
		edgeRoutes.mapNotNull { (edgeId, route) ->
			val routePx = route.map { point -> Offset(point.x * densityScale, point.y * densityScale) }
			routePx.toEdgeShape(cornerRadiusPx = edgeCornerRadiusPx, arrowHeadLengthPx = arrowHeadLengthPx)
				?.let { shape -> edgeId to shape }
		}.toMap()
	}
	var savedScale by remember(graphKey, activeSessionState) { mutableStateOf(activeSessionState.canvasScale) }
	var savedOffsetX by remember(graphKey, activeSessionState) { mutableStateOf(activeSessionState.canvasOffsetX) }
	var savedOffsetY by remember(graphKey, activeSessionState) { mutableStateOf(activeSessionState.canvasOffsetY) }
	var isMinimapToggleVisible by remember(graphKey, activeSessionState) {
		mutableStateOf(activeSessionState.isMinimapToggleVisible)
	}
	var isMinimapVisible by remember(graphKey, activeSessionState) {
		mutableStateOf(activeSessionState.isMinimapVisible)
	}
	var activeStatusFilters by remember(graphKey, activeSessionState) {
		mutableStateOf(
			activeSessionState.activeStatusFilterNames
				.mapNotNull { filterName ->
					runCatching { PensumNodeStatusType.valueOf(filterName) }.getOrNull()
				}
				.toSet()
		)
	}
	val viewport = remember(graphKey) {
		PensumCanvasViewport(
			initialScale = savedScale ?: InitialCanvasZoom,
			initialOffsetX = savedOffsetX ?: 0f,
			initialOffsetY = savedOffsetY ?: 0f
		)
	}
	val scale = viewport.scale
	val offsetX = viewport.offsetX
	val offsetY = viewport.offsetY
	// Readers of the viewport for the header and the minimap, which read it in layout and draw.
	val readScale = remember(viewport) { { viewport.scale.floatValue } }
	val readOffsetX = remember(viewport) { { viewport.offsetX.floatValue } }
	val readOffset = remember(viewport) { { viewport.offset } }
	var canvasSnapJob by remember(graphKey) { mutableStateOf<Job?>(null) }
	var manualGestureIdleJob by remember(graphKey) { mutableStateOf<Job?>(null) }
	var isDetectedManualCanvasGestureActive by remember(graphKey) { mutableStateOf(false) }
	val manualCanvasGestureActiveOverride = LocalPensumManualCanvasGestureActiveOverride.current
	val isManualCanvasGestureActive = manualCanvasGestureActiveOverride ?: isDetectedManualCanvasGestureActive

	BoxWithConstraints(
		modifier = modifier
			.fillMaxSize()
			.clipToBounds()
			.background(graphColors.screenBackground)
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
		val fitTolerancePx = with(density) { CanvasFitStateTolerance.toPx() }
		val focusState = remember(model.edges, model.nodes, selectedNodeId) {
			model.focusStateFor(selectedNodeId)
		}
		val statusFilteredNodeIds = remember(model.nodes, activeStatusFilters) {
			model.nodes
				.filter { node -> node.status.type in activeStatusFilters }
				.mapTo(mutableSetOf()) { node -> node.id }
		}
		val isStatusFilterActive = activeStatusFilters.isNotEmpty()
		val fitStateScale = fitCanvasScale(
			viewportSizePx = viewportSizePx,
			canvasSizePx = canvasSizePx,
			paddingPx = fitPaddingPx
		)
		val minimumInteractiveScale = min(fitStateScale, InitialCanvasZoom)

	fun saveCanvasViewport(scaleValue: Float, offset: Offset) {
		savedScale = scaleValue
		savedOffsetX = offset.x
		savedOffsetY = offset.y
		activeSessionState.canvasScale = scaleValue
		activeSessionState.canvasOffsetX = offset.x
		activeSessionState.canvasOffsetY = offset.y
	}

		LaunchedEffect(graphKey, viewportSizePx, canvasSizePx, panMarginPx) {
			val restoredScale = savedScale?.coerceIn(
				minimumValue = minimumInteractiveScale,
				maximumValue = MaxCanvasZoom
			) ?: InitialCanvasZoom.coerceIn(MinCanvasZoom, MaxCanvasZoom)
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
			viewport.snapTo(targetScale = restoredScale, targetOffset = constrainedOffset)
			saveCanvasViewport(
				scaleValue = restoredScale,
				offset = constrainedOffset
			)
		}

		fun animateCanvasViewport(targetScale: Float, targetOffset: Offset) {
			viewport.animateTo(
				scope = coroutineScope,
				targetScale = targetScale,
				targetOffset = targetOffset,
				durationMillis = ZoomAnimationMillis
			)
			saveCanvasViewport(
				scaleValue = targetScale,
				offset = targetOffset
			)
		}

		fun snapCanvasViewport(targetScale: Float, targetOffset: Offset) {
			canvasSnapJob?.cancel()
			viewport.snapTo(targetScale = targetScale, targetOffset = targetOffset)
			saveCanvasViewport(
				scaleValue = targetScale,
				offset = targetOffset
			)
		}

	fun revealMinimapToggle() {
		isMinimapToggleVisible = true
		activeSessionState.isMinimapToggleVisible = true
	}

	fun hideMinimap() {
		isMinimapVisible = false
		activeSessionState.isMinimapVisible = false
	}

	fun setMinimapVisible(isVisible: Boolean) {
		isMinimapVisible = isVisible
		activeSessionState.isMinimapVisible = isVisible
	}

	fun setActiveStatusFilters(filters: Set<PensumNodeStatusType>) {
		activeStatusFilters = filters
		activeSessionState.activeStatusFilterNames = filters.map { type -> type.name }.toSet()
	}

	// A gesture hides the overlays when it starts and shows them again a moment after the fingers lift: the two ends of
	// the gesture set the state, not each of its touch events.
	fun startManualCanvasGesture() {
		manualGestureIdleJob?.cancel()
		manualGestureIdleJob = null
		isDetectedManualCanvasGestureActive = true
	}

	fun endManualCanvasGesture() {
		manualGestureIdleJob?.cancel()
		manualGestureIdleJob = coroutineScope.launch {
			delay(CanvasManualGestureIdleMillis.milliseconds)
			isDetectedManualCanvasGestureActive = false
			manualGestureIdleJob = null
		}
	}

	fun toggleStatusFilter(type: PensumNodeStatusType) {
		val filters = if (type in activeStatusFilters) {
			activeStatusFilters - type
		} else {
			activeStatusFilters + type
		}
		setActiveStatusFilters(filters)
		onSelectedNodeChange(null)
	}

	fun clearStatusFilters() {
		setActiveStatusFilters(emptySet())
		onSelectedNodeChange(null)
	}

	LaunchedEffect(graphKey, selectedNodeId) {
		if (selectedNodeId != null && activeStatusFilters.isNotEmpty()) {
			setActiveStatusFilters(emptySet())
		}
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

		fun centerSelectedNode(node: PensumNodeItem) {
			centerCanvasBounds(
				bounds = node.canvasBounds(),
				targetScale = max(scale.value, NodeFocusMinZoom).coerceIn(
					minimumValue = MinCanvasZoom,
					maximumValue = MaxCanvasZoom
				)
			)
		}

		LaunchedEffect(focusRequestSerial, selectedNodeId, graphKey) {
			if (focusRequestSerial <= 0 || selectedNodeId == null) return@LaunchedEffect

			model.nodes
				.firstOrNull { node -> node.id == selectedNodeId }
				?.let { node -> centerSelectedNode(node) }
		}

		fun focusProgress() {
			onSelectedNodeChange(null)
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

		fun focusTerm(termId: String) {
			onSelectedNodeChange(null)
			val bounds = model.termFocusBounds(termId) ?: return
			centerCanvasBounds(
				bounds = bounds,
				targetScale = focusCanvasScale(
					bounds = bounds,
					viewportSizePx = viewportSizePx,
					densityScale = density.density,
					paddingPx = focusPaddingPx,
					maxScale = TermFocusMaxZoom
				)
			)
		}

		fun scheduleViewportSnap(scaleValue: Float, offset: Offset) {
			canvasSnapJob?.cancel()
			canvasSnapJob = coroutineScope.launch {
				delay(CanvasSnapDelayMillis.milliseconds)
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
			scheduleViewportSnap(
				scaleValue = scale.value,
				offset = targetOffset
			)
		}

		fun zoomTo(
			targetScale: Float,
			shouldRevealMinimapToggle: Boolean = true,
			minimumScale: Float = minimumInteractiveScale
		) {
			canvasSnapJob?.cancel()
			if (shouldRevealMinimapToggle) {
				revealMinimapToggle()
			}
			val oldScale = scale.value
			val newScale = targetScale.coerceIn(minimumScale, MaxCanvasZoom)
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

			animateCanvasViewport(
				targetScale = fitStateScale,
				targetOffset = fitCanvasOffset(fitStateScale)
			)
		}

		fun zoomIn() {
			zoomTo(
				targetScale = nextDiscreteZoomScale(
					currentScale = scale.value,
					minimumScale = minimumInteractiveScale
				),
				minimumScale = minimumInteractiveScale
			)
		}

		fun zoomOut() {
			val targetScale = previousDiscreteZoomScale(
				currentScale = scale.value,
				minimumScale = minimumInteractiveScale
			)
			if (fitStateScale <= scale.value && targetScale <= fitStateScale + CanvasFitScaleTolerance) {
				fitToScreen()
			} else {
				zoomTo(
					targetScale = targetScale,
					minimumScale = minimumInteractiveScale
				)
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

		val fitStateOffset = fitCanvasOffset(fitStateScale)
		val isFitToScreen by remember(
			fitStateScale,
			fitStateOffset,
			fitTolerancePx
		) {
			derivedStateOf {
				isCanvasFitToScreen(
					scale = scale.value,
					offset = Offset(offsetX.value, offsetY.value),
					fitScale = fitStateScale,
					fitOffset = fitStateOffset,
					offsetTolerancePx = fitTolerancePx
				)
			}
		}
		val shouldShowMinimapControls = isMinimapToggleVisible &&
			!isFitToScreen &&
			!isSubjectSheetVisible &&
			!isManualCanvasGestureActive
		ReportComposition("pensum-canvas")
		val shouldShowCanvasOverlays = !isSubjectSheetVisible && !isManualCanvasGestureActive
		// Derived, so the canvas recomposes only when the header comes or goes, or its labels switch between short and
		// full, not on every frame of a zoom.
		val shouldUseFullStickyTermLabels by remember(viewport) {
			derivedStateOf { viewport.scale.floatValue >= StickyTermHeaderFullMinZoom }
		}
		val shouldShowStickyTermHeader by remember(fitStateScale, fitStateOffset, fitTolerancePx) {
			derivedStateOf {
				shouldRenderStickyTermHeader(
					scale = scale.floatValue,
					isFitToScreen = isFitToScreen
				)
			}
		}

		LaunchedEffect(isFitToScreen) {
			if (isFitToScreen) {
				hideMinimap()
			}
		}

		// The header's click is one remembered lambda that calls the latest focusTerm, so the canvas's recompositions at
		// the start and end of a gesture hand the header the same parameters and it skips.
		val latestFocusTerm by rememberUpdatedState<(String) -> Unit> { termId -> focusTerm(termId) }
		val onStickyTermClick = remember { { termId: String -> latestFocusTerm(termId) } }

		val onNodeClick by rememberUpdatedState<(PensumNodeItem) -> Unit> { node ->
			if (node.id == selectedNodeId) {
				onFocusedNodeClick(node.id)
			} else {
				onSelectedNodeChange(node.id)
				centerSelectedNode(node)
			}
		}

		fun isNodeTap(tapOffset: Offset): Boolean {
			val canvasTapOffset = tapOffset.toCanvasOffset(
				scale = scale.value,
				offset = Offset(offsetX.value, offsetY.value)
			)
			return model.nodes.any { node -> node.containsCanvasTap(canvasTapOffset, density.density) }
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.testTag(PensumUiTags.CanvasGestureLayer)
				.pointerInput(graphKey, viewportSizePx, canvasSizePx, panMarginPx) {
					detectPensumTransformGestures(
						onTap = { tapOffset ->
							if (!isNodeTap(tapOffset)) {
								onSelectedNodeChange(null)
							}
						},
						onGestureStart = {
							canvasSnapJob?.cancel()
							startManualCanvasGesture()
							revealMinimapToggle()
						},
						onGestureEnd = {
							endManualCanvasGesture()
							scheduleViewportSnap(
								scaleValue = scale.floatValue,
								offset = viewport.offset
							)
						},
						onGesture = { centroid, pan, zoom ->
							val oldScale = scale.value
							val rawScale = oldScale * zoom
							val shouldSnapToFit = fitStateScale <= oldScale &&
								rawScale <= fitStateScale + CanvasFitScaleTolerance
							val newScale = if (shouldSnapToFit) {
								fitStateScale
							} else {
								rawScale.coerceIn(minimumInteractiveScale, MaxCanvasZoom)
							}
							val currentOffset = Offset(offsetX.value, offsetY.value)
							val nextOffset = if (shouldSnapToFit) {
								fitCanvasOffset(fitStateScale)
							} else {
								constrainCanvasOffset(
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
							}

							// Set in this frame, without a coroutine per touch event; the snap waits for the gesture's end.
							viewport.snapTo(targetScale = newScale, targetOffset = nextOffset)
							saveCanvasViewport(
								scaleValue = newScale,
								offset = nextOffset
							)
						}
					)
				}
				.pointerInput(
					graphKey,
					model.nodes,
					density.density
				) {
					detectTapGestures(
						onTap = { tapOffset ->
							if (!isNodeTap(tapOffset)) {
								onSelectedNodeChange(null)
							}
						},
						onDoubleTap = { tapOffset ->
							if (!isNodeTap(tapOffset)) {
								toggleDoubleTapZoom(tapOffset)
							}
						}
					)
				}
		) {
			Box(
				modifier = Modifier
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
				) {
					drawCanvasBackground(
						model = model,
						density = density.density,
						graphColors = graphColors
					)
				}
				model.nodes.forEach { node ->
					val isUnlockHighlighted = node.id in focusState.selectedAvailableUnlockNodeIds
					val isDimmedByFocus = focusState.isActive && node.id !in focusState.selectedFocusNodeIds
					val isDimmedByFilter = isStatusFilterActive && node.id !in statusFilteredNodeIds
					val isNodeDimmed = isDimmedByFocus || isDimmedByFilter
					// The click is remembered per node, so a recomposition of the canvas hands every card the same
					// modifier and the cards skip. Only a dimmed card gets a layer of its own, for its alpha.
					key(node.id) {
						val onClick = remember(node) { { onNodeClick(node) } }
						PensumNodeCard(
							node = node,
							isSelected = node.id == selectedNodeId,
							isRequirementHighlighted = node.id in focusState.selectedRequirementNodeIds ||
								(
									node.id in focusState.selectedUnlockNodeIds &&
										node.id !in focusState.selectedAvailableUnlockNodeIds
									),
							isUnlockHighlighted = isUnlockHighlighted,
							modifier = Modifier
								.offset(x = node.x.dp, y = node.y.dp)
								.size(width = node.width.dp, height = node.height.dp)
								.zIndex(if (isNodeDimmed) 0f else 2f)
								.then(if (isNodeDimmed) Modifier.graphicsLayer { alpha = DIMMED_NODE_ALPHA } else Modifier)
								.clickable(onClick = onClick)
								.testTag(PensumUiTags.node(node.id))
						)
					}
					if (node.id == selectedNodeId) {
						Box(
							modifier = Modifier
								.offset(x = node.x.dp, y = node.y.dp)
								.size(width = node.width.dp, height = node.height.dp)
								.zIndex(3f)
								.testTag(PensumUiTags.focusedNode(node.id))
						)
					}
				}
				Canvas(
					modifier = Modifier
						.fillMaxSize()
						.zIndex(1f)
				) {
					drawCanvasEdges(
						edgeShapes = edgeShapes,
						model = model,
						graphColors = graphColors,
						selectedRequirementEdgeIds = focusState.selectedRequirementEdgeIds,
						selectedUnlockEdgeIds = focusState.selectedUnlockEdgeIds,
						selectedAvailableUnlockEdgeIds = focusState.selectedAvailableUnlockEdgeIds,
						isFocusActive = focusState.isActive,
						statusFilteredNodeIds = statusFilteredNodeIds,
						isStatusFilterActive = isStatusFilterActive
					)
				}
			}

			if (shouldShowStickyTermHeader) {
				PensumStickyTermHeader(
					terms = model.terms,
					scale = readScale,
					offsetX = readOffsetX,
					shouldUseFullLabels = shouldUseFullStickyTermLabels,
					densityScale = density.density,
					onTermClick = onStickyTermClick,
					modifier = Modifier.align(Alignment.TopStart)
				)
			}
		}

		AnimatedVisibility(
			visible = shouldShowMinimapControls && isMinimapVisible,
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(
					start = 16.dp,
					bottom = CanvasBottomOverlayPadding + CanvasLegendOverlaySpacing
				),
			enter = canvasOverlayEnter(transformOrigin = TransformOrigin(0f, 1f)),
			exit = canvasOverlayExit(transformOrigin = TransformOrigin(0f, 1f))
		) {
			PensumMinimap(
				model = model,
				edgeRoutes = edgeRoutes,
				scale = readScale,
				offset = readOffset,
				viewportSizePx = viewportSizePx,
				selectedRequirementEdgeIds = focusState.selectedRequirementEdgeIds,
				selectedUnlockEdgeIds = focusState.selectedUnlockEdgeIds,
				selectedAvailableUnlockEdgeIds = focusState.selectedAvailableUnlockEdgeIds,
				focusedNodeIds = focusState.selectedFocusNodeIds,
				isFocusActive = focusState.isActive,
				statusFilteredNodeIds = statusFilteredNodeIds,
				isStatusFilterActive = isStatusFilterActive,
				densityScale = density.density,
				onViewportCenterChange = { canvasCenter -> moveViewportToCanvasCenter(canvasCenter) }
			)
		}

		AnimatedVisibility(
			visible = shouldShowCanvasOverlays,
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(
					end = 16.dp,
					bottom = CanvasBottomOverlayPadding + CanvasLegendOverlaySpacing
				),
			enter = canvasOverlayEnter(transformOrigin = TransformOrigin(1f, 1f)),
			exit = canvasOverlayExit(transformOrigin = TransformOrigin(1f, 1f))
		) {
			PensumZoomControls(
				isCurrentFocusVisible = model.isCurrentFocusVisible,
				isMinimapToggleVisible = shouldShowMinimapControls,
				isMinimapVisible = isMinimapVisible,
				isFitToScreenVisible = !isFitToScreen,
				onFocusProgress = { focusProgress() },
				onFitToScreen = { fitToScreen() },
					onToggleMinimap = { setMinimapVisible(!isMinimapVisible) },
				onZoomIn = { zoomIn() },
				onZoomOut = { zoomOut() }
			)
		}

		AnimatedVisibility(
			visible = shouldShowCanvasOverlays,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(bottom = CanvasBottomOverlayPadding),
			enter = canvasOverlayEnter(transformOrigin = TransformOrigin(0.5f, 1f)),
			exit = canvasOverlayExit(transformOrigin = TransformOrigin(0.5f, 1f))
		) {
			PensumCanvasLegend(
				activeStatusFilters = activeStatusFilters,
				onStatusFilterToggle = { type -> toggleStatusFilter(type) },
				onClearStatusFilters = { clearStatusFilters() },
				modifier = Modifier
			)
		}
	}
}

private suspend fun PointerInputScope.detectPensumTransformGestures(
	onTap: (Offset) -> Unit,
	onGestureStart: () -> Unit,
	onGestureEnd: () -> Unit,
	onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
	awaitEachGesture {
		val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
		var pastTouchSlop = false
		var maximumPressedPointers = 1
		val pointerEvent = awaitPointerEvent(PointerEventPass.Initial)

		// The end is told even when the gesture is cut short (the detector restarts), so the overlays always come back.
		try {
			trackPensumTransformGesture(
				firstEvent = pointerEvent,
				onSlopPassed = {
					pastTouchSlop = true
					onGestureStart()
				},
				isPastTouchSlop = { pastTouchSlop },
				onPressedPointers = { pressed -> maximumPressedPointers = max(maximumPressedPointers, pressed) },
				onGesture = onGesture
			)
		} finally {
			if (pastTouchSlop) {
				onGestureEnd()
			}
		}

		if (!pastTouchSlop && maximumPressedPointers == 1) {
			onTap(down.position)
		}
	}
}

// The touch events of one gesture, until every finger lifts: past the touch slop, each move is a pan and a zoom.
private suspend fun AwaitPointerEventScope.trackPensumTransformGesture(
	firstEvent: PointerEvent,
	onSlopPassed: () -> Unit,
	isPastTouchSlop: () -> Boolean,
	onPressedPointers: (Int) -> Unit,
	onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
	var pointerEvent = firstEvent
	var accumulatedZoom = 1f
	var accumulatedPan = Offset.Zero

	while (pointerEvent.changes.any { pointerChange -> pointerChange.pressed }) {
		onPressedPointers(pointerEvent.changes.count { pointerChange -> pointerChange.pressed })
		val zoomChange = pointerEvent.calculateZoom()
		val panChange = pointerEvent.calculatePan()

		if (!isPastTouchSlop()) {
			accumulatedZoom *= zoomChange
			accumulatedPan += panChange
			val centroidSize = pointerEvent.calculateCentroidSize(useCurrent = false)
			val zoomMotion = abs(1 - accumulatedZoom) * centroidSize
			val panMotion = accumulatedPan.getDistance()
			if (zoomMotion > viewConfiguration.touchSlop || panMotion > viewConfiguration.touchSlop) {
				onSlopPassed()
			}
		}

		if (isPastTouchSlop()) {
			val centroid = pointerEvent.calculateCentroid(useCurrent = false)
			if (zoomChange != 1f || panChange != Offset.Zero) {
				onGesture(centroid, panChange, zoomChange)
			}
			pointerEvent.consumePositionChanges()
		}

		pointerEvent = awaitPointerEvent(PointerEventPass.Initial)
	}
}

private fun PointerEvent.consumePositionChanges() {
	changes.forEach { pointerChange ->
		if (pointerChange.positionChanged()) {
			pointerChange.consume()
		}
	}
}

private fun canvasOverlayEnter(
	transformOrigin: TransformOrigin
): EnterTransition {
	return fadeIn(
		animationSpec = tween(durationMillis = CanvasOverlayAnimationMillis)
	) + scaleIn(
		animationSpec = tween(
			durationMillis = CanvasOverlayAnimationMillis,
			easing = FastOutSlowInEasing
		),
		initialScale = 0.96f,
		transformOrigin = transformOrigin
	)
}

private fun canvasOverlayExit(
	transformOrigin: TransformOrigin
): ExitTransition {
	return fadeOut(
		animationSpec = tween(durationMillis = CanvasOverlayAnimationMillis)
	) + scaleOut(
		animationSpec = tween(
			durationMillis = CanvasOverlayAnimationMillis,
			easing = FastOutSlowInEasing
		),
		targetScale = 0.96f,
		transformOrigin = transformOrigin
	)
}

private fun PensumNodeItem.containsCanvasTap(
	tapOffset: Offset,
	densityScale: Float
): Boolean {
	val left = (x * densityScale).toFloat()
	val top = (y * densityScale).toFloat()
	val right = ((x + width) * densityScale).toFloat()
	val bottom = ((y + height) * densityScale).toFloat()
	return tapOffset.x in left..right && tapOffset.y in top..bottom
}

private fun Offset.toCanvasOffset(
	scale: Float,
	offset: Offset
): Offset {
	if (scale == 0f) return Offset.Zero
	return Offset(
		x = (x - offset.x) / scale,
		y = (y - offset.y) / scale
	)
}

private fun DrawScope.drawCanvasBackground(
	model: PensumScreenModel,
	density: Float,
	graphColors: PensumGraphColors
) {
	val widthPx = model.canvas.width.toFloat() * density
	val heightPx = model.canvas.height.toFloat() * density
	drawRoundRect(
		color = graphColors.canvasBackground,
		size = Size(widthPx, heightPx),
		cornerRadius = androidx.compose.ui.geometry.CornerRadius(
			PensumElementCornerRadius.toPx(),
			PensumElementCornerRadius.toPx()
		)
	)
	model.terms.forEach { term ->
		val x = term.x.toFloat() * density
		val termWidth = term.width.toFloat() * density
		drawRect(
			color = graphColors.canvasTermBand,
			topLeft = Offset(x, 0f),
			size = Size(termWidth, heightPx)
		)
		if (x > 0f) {
			drawLine(
				color = graphColors.panelBorder,
				start = Offset(x, 0f),
				end = Offset(x, heightPx),
				strokeWidth = 1.dp.toPx()
			)
		}
	}
	val trailingTermEndPx = model.terms
		.maxOfOrNull { term -> (term.x + term.width).toFloat() * density }
		?: 0f
	if (trailingTermEndPx < widthPx) {
		drawRect(
			color = graphColors.canvasTermBand,
			topLeft = Offset(trailingTermEndPx, 0f),
			size = Size(widthPx - trailingTermEndPx, heightPx)
		)
	}
	drawRoundRect(
		color = graphColors.panelBorder,
		size = Size(widthPx, heightPx),
		cornerRadius = androidx.compose.ui.geometry.CornerRadius(
			PensumElementCornerRadius.toPx(),
			PensumElementCornerRadius.toPx()
		),
		style = Stroke(width = 1.dp.toPx())
	)
}


private fun DrawScope.drawCanvasEdges(
	edgeShapes: Map<String, PensumEdgeShape>,
	model: PensumScreenModel,
	graphColors: PensumGraphColors,
	selectedRequirementEdgeIds: Set<String>,
	selectedUnlockEdgeIds: Set<String>,
	selectedAvailableUnlockEdgeIds: Set<String>,
	isFocusActive: Boolean,
	statusFilteredNodeIds: Set<String>,
	isStatusFilterActive: Boolean
) {
	model.edges.forEach { edge ->
		val shape = edgeShapes[edge.id] ?: return@forEach
		if (
			edge.id in selectedRequirementEdgeIds ||
			edge.id in selectedUnlockEdgeIds ||
			edge.id in selectedAvailableUnlockEdgeIds
		) {
			return@forEach
		}
		val isFilteredOut = isStatusFilterActive &&
			edge.fromNodeId !in statusFilteredNodeIds &&
			edge.toNodeId !in statusFilteredNodeIds
		drawPensumEdge(
			edge = edge,
			shape = shape,
			graphColors = graphColors,
			focusTone = if (isFocusActive || isFilteredOut) EdgeFocusTone.Dimmed else EdgeFocusTone.Default
		)
	}

	model.edges.forEach { edge ->
		val shape = edgeShapes[edge.id] ?: return@forEach
		when (edge.id) {
			in selectedRequirementEdgeIds -> {
				drawPensumEdge(
					edge = edge,
					shape = shape,
					graphColors = graphColors,
					focusTone = EdgeFocusTone.Requirement
				)
			}

			in selectedUnlockEdgeIds if edge.id !in selectedRequirementEdgeIds &&
					edge.id !in selectedAvailableUnlockEdgeIds
				-> {
				drawPensumEdge(
					edge = edge,
					shape = shape,
					graphColors = graphColors,
					focusTone = EdgeFocusTone.Requirement
				)
			}

			in selectedAvailableUnlockEdgeIds if edge.id !in selectedRequirementEdgeIds
				-> {
				drawPensumEdge(
					edge = edge,
					shape = shape,
					graphColors = graphColors,
					focusTone = EdgeFocusTone.Unlock
				)
			}
		}
	}
}

private fun DrawScope.drawPensumEdge(
	edge: PensumEdgeItem,
	shape: PensumEdgeShape,
	graphColors: PensumGraphColors,
	focusTone: EdgeFocusTone
) {
	val color = when (focusTone) {
		EdgeFocusTone.Default -> graphColors.canvasNeutral
		EdgeFocusTone.Dimmed -> graphColors.canvasNeutral.copy(alpha = 0.18f)
		EdgeFocusTone.Requirement -> graphColors.selected
		EdgeFocusTone.Unlock -> graphColors.available
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
	drawPath(
		path = shape.line,
		color = color,
		style = Stroke(
			width = strokeWidth.toPx(),
			cap = StrokeCap.Round,
			pathEffect = pathEffect
		)
	)
	drawPath(path = shape.arrowHead, color = color)
}

// An edge's line and arrowhead, in canvas pixels: built once per route and drawn as they are.
private class PensumEdgeShape(
	val line: Path,
	val arrowHead: Path
)

// The shape of a route of two points or more; a shorter route has nothing to draw.
private fun List<Offset>.toEdgeShape(
	cornerRadiusPx: Float,
	arrowHeadLengthPx: Float
): PensumEdgeShape? {
	if (size < 2) return null

	return PensumEdgeShape(
		line = toRoundedOrthogonalPath(cornerRadius = cornerRadiusPx),
		arrowHead = arrowHeadPath(start = this[lastIndex - 1], end = last(), arrowLength = arrowHeadLengthPx)
	)
}

private enum class EdgeFocusTone {
	Default,
	Dimmed,
	Requirement,
	Unlock
}

private fun arrowHeadPath(start: Offset, end: Offset, arrowLength: Float): Path {
	val angle = atan2(end.y - start.y, end.x - start.x)
	val arrowAngle = PI.toFloat() / 7f
	val p1 = Offset(
		x = end.x - arrowLength * cos(angle - arrowAngle),
		y = end.y - arrowLength * sin(angle - arrowAngle)
	)
	val p2 = Offset(
		x = end.x - arrowLength * cos(angle + arrowAngle),
		y = end.y - arrowLength * sin(angle + arrowAngle)
	)
	return Path().apply {
		moveTo(end.x, end.y)
		lineTo(p1.x, p1.y)
		lineTo(p2.x, p2.y)
		close()
	}
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

private fun isCanvasFitToScreen(
	scale: Float,
	offset: Offset,
	fitScale: Float,
	fitOffset: Offset,
	offsetTolerancePx: Float
): Boolean {
	return abs(scale - fitScale) <= CanvasFitScaleTolerance &&
		abs(offset.x - fitOffset.x) <= offsetTolerancePx &&
		abs(offset.y - fitOffset.y) <= offsetTolerancePx
}

internal fun shouldRenderStickyTermHeader(
	scale: Float,
	isFitToScreen: Boolean
): Boolean {
	return !isFitToScreen && scale >= StickyTermHeaderShortMinZoom
}

private fun nextDiscreteZoomScale(
	currentScale: Float,
	minimumScale: Float
): Float {
	val levels = discreteZoomLevels(minimumScale)
	return levels.firstOrNull { level -> level > currentScale + CanvasFitScaleTolerance } ?: levels.last()
}

private fun previousDiscreteZoomScale(
	currentScale: Float,
	minimumScale: Float
): Float {
	val levels = discreteZoomLevels(minimumScale)
	return levels.lastOrNull { level -> level < currentScale - CanvasFitScaleTolerance } ?: levels.first()
}

private fun discreteZoomLevels(minimumScale: Float): List<Float> {
	val minScale = minimumScale.coerceIn(MinCanvasFitZoom, MaxCanvasZoom)
	if (minScale >= MaxCanvasZoom) return listOf(MaxCanvasZoom)

	val ratio = MaxCanvasZoom.toDouble() / minScale.toDouble()
	return (0..ZoomControlStepCount).map { index ->
		val progress = index.toDouble() / ZoomControlStepCount.toDouble()
		(minScale.toDouble() * ratio.pow(progress)).toFloat()
	}
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

private fun PensumNodeItem.canvasBounds(): CanvasBounds {
	return CanvasBounds(
		left = x.toFloat(),
		top = y.toFloat(),
		right = (x + width).toFloat(),
		bottom = (y + height).toFloat()
	)
}

private fun List<PensumNodeItem>.canvasBounds(): CanvasBounds? {
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
	return currentNodes.canvasBounds()
}

private fun PensumScreenModel.termFocusBounds(termId: String): CanvasBounds? {
	val term = terms.firstOrNull { term -> term.id == termId } ?: return null
	val termNodes = nodes.filter { node -> node.termId == termId }
	val nodeBounds = termNodes.canvasBounds()
	if (nodeBounds != null) {
		return CanvasBounds(
			left = min(term.x.toFloat(), nodeBounds.left),
			top = nodeBounds.top,
			right = max((term.x + term.width).toFloat(), nodeBounds.right),
			bottom = nodeBounds.bottom
		)
	}

	return CanvasBounds(
		left = term.x.toFloat(),
		top = 0f,
		right = (term.x + term.width).toFloat(),
		bottom = canvas.height.toFloat()
	)
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
		return offset.coerceIn(
			minimumValue = -panMarginPx,
			maximumValue = viewportSizePx - contentSizePx + panMarginPx
		)
	}

	return offset.coerceIn(
		minimumValue = viewportSizePx - contentSizePx - panMarginPx,
		maximumValue = panMarginPx
	)
}

private const val DIMMED_NODE_ALPHA = 0.34f
