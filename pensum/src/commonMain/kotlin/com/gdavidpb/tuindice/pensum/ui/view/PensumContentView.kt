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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.dialog.PensumSelectionBottomSheet
import com.gdavidpb.tuindice.pensum.ui.dialog.PensumSubjectDetailBottomSheet
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationDirection

@Composable
fun PensumContentView(
	model: PensumScreenModel,
	isRefreshing: Boolean,
	localDataMessage: UiText?,
	showSelectionSheet: Boolean,
	screenSessionStore: PensumScreenSessionStore,
	isSummaryCollapsed: Boolean,
	onSummaryCollapsedToggle: () -> Unit,
	onSelectionSheetDismiss: () -> Unit,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onSelectionApplied: (PensumOptionItem, PensumModalityItem) -> Unit,
	onPensumContextClick: () -> Unit
) {
	val graphColors = pensumGraphColors()
	val pensumStateKey = "${model.selection.year}-${model.selection.modalityId}"
	val selectionSessionState = remember(screenSessionStore, pensumStateKey) {
		screenSessionStore.stateFor(pensumStateKey)
	}
	val focusedNodeIdState = remember(pensumStateKey, selectionSessionState) {
		mutableStateOf(selectionSessionState.focusedNodeId)
	}
	val detailNodeIdState = remember(pensumStateKey, selectionSessionState) {
		mutableStateOf(selectionSessionState.detailNodeId)
	}
	val shouldOpenDetailExpandedState = remember(pensumStateKey, selectionSessionState) {
		mutableStateOf(selectionSessionState.shouldOpenDetailExpanded)
	}
	val detailNavigationOriginNodeIdState = remember(pensumStateKey, selectionSessionState) {
		mutableStateOf(selectionSessionState.detailNavigationOriginNodeId)
	}
	val detailNavigationDirectionNameState = remember(pensumStateKey, selectionSessionState) {
		mutableStateOf(selectionSessionState.detailNavigationDirectionName)
	}
	val focusRequestSerialState = remember(pensumStateKey, selectionSessionState) {
		mutableStateOf(selectionSessionState.focusRequestSerial)
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

	fun setFocusedNodeId(nodeId: String?) {
		focusedNodeIdState.value = nodeId
		selectionSessionState.focusedNodeId = nodeId
	}

	fun setDetailNodeId(nodeId: String?) {
		detailNodeIdState.value = nodeId
		selectionSessionState.detailNodeId = nodeId
	}

	fun setShouldOpenDetailExpanded(shouldOpen: Boolean) {
		shouldOpenDetailExpandedState.value = shouldOpen
		selectionSessionState.shouldOpenDetailExpanded = shouldOpen
	}

	fun setDetailNavigationOriginNodeId(nodeId: String?) {
		detailNavigationOriginNodeIdState.value = nodeId
		selectionSessionState.detailNavigationOriginNodeId = nodeId
	}

	fun setDetailNavigationDirectionName(directionName: String?) {
		detailNavigationDirectionNameState.value = directionName
		selectionSessionState.detailNavigationDirectionName = directionName
	}

	fun incrementFocusRequestSerial() {
		val nextSerial = focusRequestSerialState.value + 1
		focusRequestSerialState.value = nextSerial
		selectionSessionState.focusRequestSerial = nextSerial
	}

	fun clearSubjectDetail() {
		setDetailNodeId(null)
		setShouldOpenDetailExpanded(false)
		setDetailNavigationOriginNodeId(null)
		setDetailNavigationDirectionName(null)
	}

	fun clearSubjectContext() {
		setFocusedNodeId(null)
		clearSubjectDetail()
	}

	LaunchedEffect(showSelectionSheet) {
		if (showSelectionSheet) {
			clearSubjectContext()
		}
	}

	LaunchedEffect(nodeIds, focusedNodeIdState.value, detailNodeIdState.value) {
		if (focusedNodeIdState.value != null && focusedNodeIdState.value !in nodeIds) {
			setFocusedNodeId(null)
		}
		if (detailNodeIdState.value != null && detailNodeIdState.value !in nodeIds) {
			clearSubjectDetail()
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
			isCollapsed = isSummaryCollapsed,
			onSummaryClick = onSummaryCollapsedToggle,
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
		AnimatedVisibility(
			visible = localDataMessage != null && !isRefreshing,
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
				PensumLocalDataWarningView(message = localDataMessage?.asString().orEmpty())
			}
		}
		PensumGraphCanvas(
			model = model,
			sessionState = selectionSessionState,
			selectedNodeId = if (showSelectionSheet) null else focusedNodeId,
			onSelectedNodeChange = { nodeId ->
				setFocusedNodeId(nodeId)
				setDetailNodeId(nodeId)
				setShouldOpenDetailExpanded(false)
				setDetailNavigationOriginNodeId(null)
				setDetailNavigationDirectionName(null)
			},
			onFocusedNodeClick = { nodeId ->
				setDetailNodeId(nodeId)
				setShouldOpenDetailExpanded(false)
				setDetailNavigationOriginNodeId(null)
				setDetailNavigationDirectionName(null)
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
			onSubjectStatsClick = { subjectCode ->
				clearSubjectContext()
				onSubjectStatsClick(subjectCode)
			},
			onRelatedSubjectClick = { target ->
				setDetailNavigationOriginNodeId(target.originNodeId)
				setDetailNavigationDirectionName(target.direction.name)
				setFocusedNodeId(target.nodeId)
				setDetailNodeId(target.nodeId)
				setShouldOpenDetailExpanded(true)
				incrementFocusRequestSerial()
			},
			onDismissRequest = {
				clearSubjectDetail()
			}
		)
	}
}
