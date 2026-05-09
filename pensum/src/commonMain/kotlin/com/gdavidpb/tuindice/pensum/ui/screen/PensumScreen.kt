package com.gdavidpb.tuindice.pensum.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.EmptyView
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_empty_message
import tuindice.pensum.generated.resources.pensum_empty_title
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.pensum_failed_retry
import tuindice.pensum.generated.resources.pensum_failed_title
import tuindice.pensum.generated.resources.pensum_selection_apply
import tuindice.pensum.generated.resources.pensum_selection_cancel
import tuindice.pensum.generated.resources.pensum_selection_current
import tuindice.pensum.generated.resources.pensum_selection_modality
import tuindice.pensum.generated.resources.pensum_selection_title
import tuindice.pensum.generated.resources.pensum_selection_version
import tuindice.pensum.generated.resources.pensum_progress_label
import tuindice.pensum.generated.resources.pensum_subject_stats_content_description
import tuindice.pensum.generated.resources.pensum_zoom_in
import tuindice.pensum.generated.resources.pensum_zoom_out

private val ScreenBackground = Color(0xFF101112)
private val PanelBackground = Color(0xFF171819)
private val PanelBorder = Color(0xFF343638)
private val Approved = Color(0xFF8FE38C)
private val Current = Color(0xFFFFC400)
private val Available = Color(0xFF8A8F94)
private val Blocked = Color(0xFF686B70)
private val Selected = Color(0xFFF7F7F7)
private val TextPrimary = Color(0xFFF7F7F7)
private val TextSecondary = Color(0xFF9C9EA3)

private const val MinCanvasZoom = 0.35f
private const val MaxCanvasZoom = 2.25f
private const val InitialCanvasZoom = 0.74f
private const val ZoomButtonStep = 0.18f
private const val ZoomAnimationMillis = 220
private const val SummaryAnimationMillis = 700
private val InitialCanvasOffset = 18.dp
private val CanvasPanMargin = 36.dp
private val MinimapWidth = 156.dp
private val MinimapHeight = 104.dp
private val GraphControlsGap = 6.dp
private val EdgeEndpointGap = 0.dp
private val EdgeCornerRadius = 14.dp
private val EdgeRerouteSpacing = 32.dp
private val ArrowHeadLength = 12.dp

@Composable
fun PensumScreen(
	state: Pensum.State,
	onRetryClick: () -> Unit,
	showSelectionSheet: Boolean,
	onSelectionSheetDismiss: () -> Unit,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onSelectionApplied: (PensumScreenModel.PensumOptionItem, PensumScreenModel.ModalityItem) -> Unit
) {
	SealedCrossfade(targetState = state) { targetState ->
		when (targetState) {
			is Pensum.State.Loading -> PensumLoadingView()
			is Pensum.State.Empty -> PensumEmptyView()
			is Pensum.State.Content -> PensumContentView(
				model = targetState.model,
				showSelectionSheet = showSelectionSheet,
				onSelectionSheetDismiss = onSelectionSheetDismiss,
				onSubjectStatsClick = onSubjectStatsClick,
				onSelectionApplied = onSelectionApplied
			)
			is Pensum.State.Failed -> ErrorView(
				title = stringResource(Res.string.pensum_failed_title),
				message = stringResource(Res.string.pensum_failed_message),
				retryText = stringResource(Res.string.pensum_failed_retry),
				onRetryClick = onRetryClick,
				headerContent = { ErrorStateAnimationView() }
			)
		}
	}
}

@Composable
private fun PensumEmptyView() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
	) {
		EmptyView(
			title = stringResource(Res.string.pensum_empty_title),
			message = stringResource(Res.string.pensum_empty_message),
			headerContent = { EmptyStateAnimationView() }
		)
	}
}

@Composable
private fun PensumLoadingView() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
	) {
		CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
	}
}

@Composable
private fun PensumContentView(
	model: PensumScreenModel,
	showSelectionSheet: Boolean,
	onSelectionSheetDismiss: () -> Unit,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onSelectionApplied: (PensumScreenModel.PensumOptionItem, PensumScreenModel.ModalityItem) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
			.testTag(PensumUiTags.PensumScreen)
	) {
		PensumSummaryRow(model = model)
		PensumGraphCanvas(
			model = model,
			onSubjectStatsClick = onSubjectStatsClick,
			modifier = Modifier.weight(1f)
		)
	}

	if (showSelectionSheet) {
		PensumSelectionBottomSheet(
			model = model,
			onSelectionApplied = onSelectionApplied,
			onDismissRequest = onSelectionSheetDismiss
		)
	}
}

@Composable
private fun PensumSummaryRow(model: PensumScreenModel) {
	val selectedPensum = model.pensumOptions.firstOrNull { item ->
		item.careerCode == model.selection.careerCode && item.year == model.selection.year
	}
	val selectedModality = model.modalityOptions.firstOrNull { item -> item.id == model.selection.modalityId }
	val contextText = listOfNotNull(
		selectedPensum?.let { item -> "${item.careerName} ${item.year}" },
		selectedModality?.name
	).joinToString(separator = " · ")
	val progress = remember { Animatable(0f) }
	val approvedCredits = remember { Animatable(0f) }
	val totalCredits = remember { Animatable(0f) }
	val targetProgress = model.progressPercent.coerceIn(0, 100) / 100f
	val summaryAnimationSpec = tween<Float>(
		durationMillis = SummaryAnimationMillis,
		easing = DecelerateEasing
	)

	LaunchedEffect(
		model.progressPercent,
		model.approvedCredits,
		model.totalCredits
	) {
		launch {
			progress.animateTo(
				targetValue = targetProgress,
				animationSpec = summaryAnimationSpec
			)
		}
		launch {
			approvedCredits.animateTo(
				targetValue = model.approvedCredits.toFloat(),
				animationSpec = summaryAnimationSpec
			)
		}
		launch {
			totalCredits.animateTo(
				targetValue = model.totalCredits.toFloat(),
				animationSpec = summaryAnimationSpec
			)
		}
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(ScreenBackground)
			.padding(horizontal = 16.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(10.dp)
	) {
		ProgressRing(progress = progress.value)
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = "${(progress.value * 100).roundToInt()}% ${stringResource(Res.string.pensum_progress_label)}",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold,
				color = TextPrimary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = "${approvedCredits.value.roundToInt()}/${totalCredits.value.roundToInt()} UC",
				style = MaterialTheme.typography.bodyMedium,
				color = TextSecondary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}

	if (contextText.isNotBlank()) {
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.background(ScreenBackground)
				.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
				.background(PanelBackground, RoundedCornerShape(50))
				.border(1.dp, PanelBorder, RoundedCornerShape(50))
				.padding(horizontal = 10.dp, vertical = 6.dp),
			text = contextText,
			style = MaterialTheme.typography.labelMedium,
			color = TextSecondary,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun ProgressRing(progress: Float) {
	Canvas(modifier = Modifier.size(24.dp)) {
		drawCircle(color = PanelBorder, style = Stroke(width = 4.dp.toPx()))
		drawArc(
			color = Approved,
			startAngle = -90f,
			sweepAngle = 360f * progress.coerceIn(0f, 1f),
			useCenter = false,
			style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PensumSelectionBottomSheet(
	model: PensumScreenModel,
	onSelectionApplied: (PensumScreenModel.PensumOptionItem, PensumScreenModel.ModalityItem) -> Unit,
	onDismissRequest: () -> Unit
) {
	val currentPensum = model.selectedPensumOption() ?: model.pensumOptions.firstOrNull()
	val currentModality = model.selectedModality() ?: model.modalityOptions.firstOrNull()
	if (currentPensum == null || currentModality == null) return

	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val selectedPensumState = remember(currentPensum.id) {
		mutableStateOf(currentPensum)
	}
	val selectedModalityState = remember(currentModality.id) {
		mutableStateOf(currentModality)
	}
	val currentSelectionText = stringResource(
		Res.string.pensum_selection_current,
		currentPensum.careerName,
		currentPensum.year,
		currentModality.name
	)

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(Res.string.pensum_selection_title),
		positiveText = stringResource(Res.string.pensum_selection_apply),
		negativeText = stringResource(Res.string.pensum_selection_cancel),
		onPositiveClick = {
			val selectedPensum = selectedPensumState.value
			val selectedModality = selectedModalityState.value
			val isSelectionChanged =
				selectedPensum.careerCode != model.selection.careerCode ||
					selectedPensum.year != model.selection.year ||
					selectedModality.id != model.selection.modalityId

			if (isSelectionChanged) {
				onSelectionApplied(selectedPensum, selectedModality)
			}
		},
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = currentSelectionText,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(Res.string.pensum_selection_version),
					style = MaterialTheme.typography.titleSmall,
					fontWeight = FontWeight.SemiBold
				)
				LazyRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(
						items = model.pensumOptions,
						key = PensumScreenModel.PensumOptionItem::id
					) { item ->
						FilterChip(
							selected = item.id == selectedPensumState.value.id,
							onClick = { selectedPensumState.value = item },
							label = {
								Text(
									text = item.year.toString(),
									maxLines = 1
								)
							},
							colors = FilterChipDefaults.filterChipColors(
								selectedContainerColor = Current.copy(alpha = 0.18f),
								selectedLabelColor = MaterialTheme.colorScheme.onSurface
							)
						)
					}
				}
			}

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(Res.string.pensum_selection_modality),
					style = MaterialTheme.typography.titleSmall,
					fontWeight = FontWeight.SemiBold
				)
				model.modalityOptions.forEach { modality ->
					PensumModalityOptionRow(
						modality = modality,
						isSelected = modality.id == selectedModalityState.value.id,
						onClick = { selectedModalityState.value = modality }
					)
				}
			}
		}
	}
}

@Composable
private fun PensumModalityOptionRow(
	modality: PensumScreenModel.ModalityItem,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(8.dp),
		color = if (isSelected)
			MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
		else
			MaterialTheme.colorScheme.surface,
		border = androidx.compose.foundation.BorderStroke(
			width = 1.dp,
			color = if (isSelected)
				MaterialTheme.colorScheme.primary
			else
				MaterialTheme.colorScheme.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				modifier = Modifier.weight(1f),
				text = modality.name,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			if (isSelected) {
				Icon(
					imageVector = Icons.Filled.Check,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}

@Composable
private fun PensumGraphCanvas(
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
			ZoomControls(
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

@Composable
private fun PensumNodeCard(
	node: PensumScreenModel.Node,
	isSelected: Boolean,
	isRequirementHighlighted: Boolean,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val colors = node.status.colors()
	val isHighlighted = isSelected || isRequirementHighlighted
	val subjectCode = node.subjectCode
	val chipColors = remember(node.displayCode, colors.chip, colors.chipText) {
		node.displayCode.toPensumChipColors(
			fallbackContainer = colors.chip,
			fallbackContent = colors.chipText
		)
	}
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(8.dp),
		color = colors.container,
		border = androidx.compose.foundation.BorderStroke(
			width = if (isHighlighted) 2.2.dp else 1.2.dp,
			color = if (isHighlighted) Selected else colors.border
		),
		shadowElevation = if (isSelected || node.status == PensumNodeStatus.CURRENT) 8.dp else 0.dp
	) {
		Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
			Column(
				modifier = Modifier.align(Alignment.TopStart)
			) {
				Text(
					modifier = Modifier
						.background(chipColors.container, RoundedCornerShape(6.dp))
						.padding(horizontal = 8.dp, vertical = 4.dp),
					text = node.displayCode,
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.SemiBold,
					color = chipColors.content,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(10.dp))
				Text(
					text = node.name,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = if (node.status == PensumNodeStatus.CURRENT) FontWeight.Bold else FontWeight.Medium,
					color = colors.text,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = "${node.credits} UC",
					style = MaterialTheme.typography.bodyMedium,
					color = colors.secondaryText
				)
			}
			if (node.status == PensumNodeStatus.APPROVED) {
				Box(
					modifier = Modifier
						.align(Alignment.TopEnd)
						.size(22.dp)
						.background(PanelBackground, CircleShape)
						.border(1.4.dp, Approved, CircleShape),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Filled.Check,
						contentDescription = null,
						tint = Approved,
						modifier = Modifier.size(16.dp)
					)
				}
			}
			if (node.hasSubjectStatsAction && subjectCode != null) {
				IconButton(
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.size(32.dp)
						.testTag(PensumUiTags.nodeSubjectStatsButton(node.id)),
					onClick = { onSubjectStatsClick(subjectCode) }
				) {
					Icon(
						imageVector = Icons.Outlined.BarChart,
						contentDescription = stringResource(
							Res.string.pensum_subject_stats_content_description,
							subjectCode
						),
						tint = colors.secondaryText,
						modifier = Modifier.size(20.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun PensumMinimap(
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
				color = node.status.colors().border,
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

@Composable
private fun ZoomControls(
	onZoomIn: () -> Unit,
	onZoomOut: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.width(48.dp)
			.height(MinimapHeight)
			.background(Color.Black.copy(alpha = 0.68f), RoundedCornerShape(8.dp))
			.border(1.dp, Available, RoundedCornerShape(8.dp))
	) {
		IconButton(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.testTag(PensumUiTags.ZoomIn),
			onClick = onZoomIn
		) {
			Icon(
				imageVector = Icons.Filled.Add,
				contentDescription = stringResource(Res.string.pensum_zoom_in),
				tint = TextPrimary
			)
		}
		Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Available.copy(alpha = 0.4f)))
		IconButton(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.testTag(PensumUiTags.ZoomOut),
			onClick = onZoomOut
		) {
			Icon(
				imageVector = Icons.Filled.Remove,
				contentDescription = stringResource(Res.string.pensum_zoom_out),
				tint = TextPrimary
			)
		}
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

private fun PensumScreenModel.selectedPensumOption(): PensumScreenModel.PensumOptionItem? {
	return pensumOptions.firstOrNull { item ->
		item.careerCode == selection.careerCode && item.year == selection.year
	}
}

private fun PensumScreenModel.selectedModality(): PensumScreenModel.ModalityItem? {
	return modalityOptions.firstOrNull { item -> item.id == selection.modalityId }
}

private fun PensumScreenModel.requirementEdgeIdsTo(nodeId: String?): Set<String> {
	if (nodeId == null) return emptySet()

	val incomingRequirementEdges = edges
		.filter { edge -> edge.relationshipType == PensumRelationshipType.REQUIREMENT }
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

private fun PensumScreenModel.edgeRoute(
	edge: PensumScreenModel.Edge,
	endpointGap: Float,
	rerouteSpacing: Float
): List<Offset> {
	val from = nodes.firstOrNull { node -> node.id == edge.fromNodeId }
	val to = nodes.firstOrNull { node -> node.id == edge.toNodeId }
	if (from == null || to == null) {
		return edge.points.map { point -> Offset(point.x.toFloat(), point.y.toFloat()) }
	}

	val fromCenter = from.center
	val toCenter = to.center
	val dx = toCenter.x - fromCenter.x
	val dy = toCenter.y - fromCenter.y
	val shouldUseVerticalRoute = abs(dy) > abs(dx) * 0.85f && abs(dy) > from.height.toFloat()

	return if (shouldUseVerticalRoute) {
		val direction = if (dy >= 0f) 1f else -1f
		val start = Offset(
			x = fromCenter.x,
			y = if (direction > 0f) from.bottom + endpointGap else from.y.toFloat() - endpointGap
		)
		val end = Offset(
			x = toCenter.x,
			y = if (direction > 0f) to.y.toFloat() - endpointGap else to.bottom + endpointGap
		)
		val routeGap = (end.y - start.y) * direction
		val turnY = if (routeGap >= 0f) {
			(start.y + end.y) / 2f
		} else if (direction > 0f) {
			max(from.bottom, to.bottom) + rerouteSpacing
		} else {
			min(from.y.toFloat(), to.y.toFloat()) - rerouteSpacing
		}
		if (abs(start.x - end.x) < 1f) {
			listOf(start, end)
		} else {
			listOf(
				start,
				Offset(start.x, turnY),
				Offset(end.x, turnY),
				end
			)
		}
	} else {
		val direction = if (dx >= 0f) 1f else -1f
		val start = Offset(
			x = if (direction > 0f) from.right + endpointGap else from.x.toFloat() - endpointGap,
			y = fromCenter.y
		)
		val end = Offset(
			x = if (direction > 0f) to.x.toFloat() - endpointGap else to.right + endpointGap,
			y = toCenter.y
		)
		val routeGap = (end.x - start.x) * direction
		val turnX = if (routeGap >= 0f) {
			(start.x + end.x) / 2f
		} else if (direction > 0f) {
			max(from.right, to.right) + rerouteSpacing
		} else {
			min(from.x.toFloat(), to.x.toFloat()) - rerouteSpacing
		}
		if (abs(start.y - end.y) < 1f) {
			listOf(start, end)
		} else {
			listOf(
				start,
				Offset(turnX, start.y),
				Offset(turnX, end.y),
				end
			)
		}
	}
}

private val PensumScreenModel.Node.center: Offset
	get() = Offset(
		x = x.toFloat() + width.toFloat() / 2f,
		y = y.toFloat() + height.toFloat() / 2f
	)

private val PensumScreenModel.Node.right: Float
	get() = x.toFloat() + width.toFloat()

private val PensumScreenModel.Node.bottom: Float
	get() = y.toFloat() + height.toFloat()

private fun List<Offset>.toRoundedOrthogonalPath(cornerRadius: Float): Path {
	val compactPoints = withoutNearDuplicates()
	return Path().apply {
		if (compactPoints.isEmpty()) return@apply
		moveTo(compactPoints.first().x, compactPoints.first().y)
		if (compactPoints.size == 1) return@apply

		for (index in 1 until compactPoints.lastIndex) {
			val previous = compactPoints[index - 1]
			val current = compactPoints[index]
			val next = compactPoints[index + 1]
			if (!current.isOrthogonalTurn(previous, next)) {
				lineTo(current.x, current.y)
				continue
			}

			val radius = min(
				cornerRadius,
				min(previous.distanceTo(current) / 2f, current.distanceTo(next) / 2f)
			)
			val beforeCorner = current.toward(previous, radius)
			val afterCorner = current.toward(next, radius)
			lineTo(beforeCorner.x, beforeCorner.y)
			quadraticTo(current.x, current.y, afterCorner.x, afterCorner.y)
		}
		lineTo(compactPoints.last().x, compactPoints.last().y)
	}
}

private fun List<Offset>.withoutNearDuplicates(): List<Offset> {
	return fold(emptyList()) { points, point ->
		if (points.lastOrNull()?.distanceTo(point)?.let { distance -> distance < 0.5f } == true) {
			points
		} else {
			points + point
		}
	}
}

private fun Offset.isOrthogonalTurn(previous: Offset, next: Offset): Boolean {
	val incomingHorizontal = abs(x - previous.x) > 0.5f && abs(y - previous.y) < 0.5f
	val incomingVertical = abs(y - previous.y) > 0.5f && abs(x - previous.x) < 0.5f
	val outgoingHorizontal = abs(next.x - x) > 0.5f && abs(next.y - y) < 0.5f
	val outgoingVertical = abs(next.y - y) > 0.5f && abs(next.x - x) < 0.5f
	return (incomingHorizontal && outgoingVertical) || (incomingVertical && outgoingHorizontal)
}

private fun Offset.toward(target: Offset, distance: Float): Offset {
	val dx = target.x - x
	val dy = target.y - y
	val length = sqrt(dx * dx + dy * dy)
	if (length <= 0.5f) return this
	val ratio = (distance / length).coerceIn(0f, 1f)
	return Offset(
		x = x + dx * ratio,
		y = y + dy * ratio
	)
}

private fun Offset.distanceTo(other: Offset): Float {
	val dx = other.x - x
	val dy = other.y - y
	return sqrt(dx * dx + dy * dy)
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

private data class NodeColors(
	val container: Color,
	val border: Color,
	val chip: Color,
	val chipText: Color,
	val text: Color,
	val secondaryText: Color
)

private data class PensumChipColors(
	val container: Color,
	val content: Color
)

private fun String.toPensumChipColors(
	fallbackContainer: Color,
	fallbackContent: Color
): PensumChipColors {
	return CourseCodeColorGenerator.fromCodeOrNull(this)
		?.let { PensumChipColors(container = it.containerColor, content = it.color) }
		?: PensumChipColors(container = fallbackContainer, content = fallbackContent)
}

private fun PensumNodeStatus.colors(): NodeColors {
	return when (this) {
		PensumNodeStatus.APPROVED -> NodeColors(
			container = PanelBackground,
			border = Approved,
			chip = Color(0xFFB8F4A8),
			chipText = Color(0xFF1D5B25),
			text = TextPrimary,
			secondaryText = TextSecondary
		)
		PensumNodeStatus.CURRENT -> NodeColors(
			container = PanelBackground,
			border = Current,
			chip = Color(0xFFF7E6A6),
			chipText = Color(0xFF5A4A00),
			text = TextPrimary,
			secondaryText = TextSecondary
		)
		PensumNodeStatus.AVAILABLE -> NodeColors(
			container = PanelBackground,
			border = Available,
			chip = Color(0xFFEBDDA3),
			chipText = Color(0xFF534500),
			text = TextPrimary,
			secondaryText = TextSecondary
		)
		PensumNodeStatus.BLOCKED -> NodeColors(
			container = Color(0xFF242628),
			border = Blocked,
			chip = Color(0xFFB7B8BA),
			chipText = Color(0xFF383A3D),
			text = Color(0xFFC7C8CA),
			secondaryText = Color(0xFF8A8C90)
		)
	}
}
