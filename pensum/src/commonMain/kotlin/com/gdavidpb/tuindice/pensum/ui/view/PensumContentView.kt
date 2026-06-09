package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.dialog.PensumSelectionBottomSheet
import com.gdavidpb.tuindice.pensum.ui.dialog.PensumSubjectDetailBottomSheet
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationDirection

@Composable
fun PensumContentView(
	model: PensumScreenModel,
	isRefreshing: Boolean,
	showSelectionSheet: Boolean,
	onSelectionSheetDismiss: () -> Unit,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onSelectionApplied: (PensumOptionItem, PensumModalityItem) -> Unit,
	onPensumContextClick: () -> Unit
) {
	val graphColors = pensumGraphColors()
	val pensumStateKey = "${model.selection.year}-${model.selection.modalityId}"
	val focusedNodeIdState = rememberSaveable(pensumStateKey) {
		mutableStateOf<String?>(null)
	}
	val detailNodeIdState = rememberSaveable(pensumStateKey) {
		mutableStateOf<String?>(null)
	}
	val shouldOpenDetailExpandedState = rememberSaveable(pensumStateKey) {
		mutableStateOf(false)
	}
	val detailNavigationOriginNodeIdState = rememberSaveable(pensumStateKey) {
		mutableStateOf<String?>(null)
	}
	val detailNavigationDirectionNameState = rememberSaveable(pensumStateKey) {
		mutableStateOf<String?>(null)
	}
	val focusRequestSerialState = remember(model.selection.year, model.selection.modalityId) {
		mutableStateOf(0)
	}
	val nodeIds = remember(model.nodes) {
		model.nodes.mapTo(mutableSetOf()) { node -> node.id }
	}
	val focusedNodeId = focusedNodeIdState.value?.takeIf { nodeId -> nodeId in nodeIds }
	val detailNodeId = detailNodeIdState.value?.takeIf { nodeId -> nodeId in nodeIds }
	val detailNavigationDirection = detailNavigationDirectionNameState.value
		?.let(PensumSubjectDetailNavigationDirection::valueOf)
	val detailNode = if (showSelectionSheet) {
		null
	} else {
		model.nodes.firstOrNull { node -> node.id == detailNodeId }
	}
	val isSubjectDetailVisible = detailNode != null

	fun clearSubjectContext() {
		focusedNodeIdState.value = null
		detailNodeIdState.value = null
		shouldOpenDetailExpandedState.value = false
		detailNavigationOriginNodeIdState.value = null
		detailNavigationDirectionNameState.value = null
	}

	LaunchedEffect(showSelectionSheet) {
		if (showSelectionSheet) {
			clearSubjectContext()
		}
	}

	LaunchedEffect(nodeIds, focusedNodeIdState.value, detailNodeIdState.value) {
		if (focusedNodeIdState.value != null && focusedNodeIdState.value !in nodeIds) {
			focusedNodeIdState.value = null
		}
		if (detailNodeIdState.value != null && detailNodeIdState.value !in nodeIds) {
			detailNodeIdState.value = null
			shouldOpenDetailExpandedState.value = false
			detailNavigationOriginNodeIdState.value = null
			detailNavigationDirectionNameState.value = null
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(graphColors.screenBackground)
			.testTag(PensumUiTags.PensumScreen)
	) {
		PensumSummaryRow(
			model = model,
			onPensumContextClick = {
				clearSubjectContext()
				onPensumContextClick()
			}
		)
		AnimatedVisibility(
			visible = isRefreshing,
			enter = fadeIn(
				animationSpec = tween(durationMillis = CanvasOverlayAnimationMillis)
			) + expandVertically(
				expandFrom = Alignment.Top,
				animationSpec = tween(durationMillis = CanvasOverlayAnimationMillis)
			),
			exit = fadeOut(
				animationSpec = tween(durationMillis = CanvasOverlayAnimationMillis)
			) + shrinkVertically(
				shrinkTowards = Alignment.Top,
				animationSpec = tween(durationMillis = CanvasOverlayAnimationMillis)
			)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				PensumRefreshingIndicatorView()
			}
		}
		PensumGraphCanvas(
			model = model,
			selectedNodeId = if (showSelectionSheet) null else focusedNodeId,
			onSelectedNodeChange = { nodeId ->
				focusedNodeIdState.value = nodeId
				detailNodeIdState.value = nodeId
				shouldOpenDetailExpandedState.value = false
				detailNavigationOriginNodeIdState.value = null
				detailNavigationDirectionNameState.value = null
			},
			isSubjectSheetVisible = isSubjectDetailVisible,
			focusRequestSerial = focusRequestSerialState.value,
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

	if (detailNode != null) {
		PensumSubjectDetailBottomSheet(
			node = detailNode,
			shouldStartExpanded = shouldOpenDetailExpandedState.value,
			navigationOriginNodeId = detailNavigationOriginNodeIdState.value,
			navigationDirection = detailNavigationDirection,
			onSubjectStatsClick = onSubjectStatsClick,
			onRelatedSubjectClick = { target ->
				detailNavigationOriginNodeIdState.value = target.originNodeId
				detailNavigationDirectionNameState.value = target.direction.name
				focusedNodeIdState.value = target.nodeId
				detailNodeIdState.value = target.nodeId
				shouldOpenDetailExpandedState.value = true
				focusRequestSerialState.value += 1
			},
			onDismissRequest = {
				detailNodeIdState.value = null
				shouldOpenDetailExpandedState.value = false
				detailNavigationOriginNodeIdState.value = null
				detailNavigationDirectionNameState.value = null
			}
		)
	}
}
