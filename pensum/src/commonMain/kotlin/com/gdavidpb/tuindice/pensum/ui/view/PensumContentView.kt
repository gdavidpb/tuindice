package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.dialog.PensumSelectionBottomSheet
import com.gdavidpb.tuindice.pensum.ui.dialog.PensumSubjectDetailBottomSheet

@Composable
fun PensumContentView(
	model: PensumScreenModel,
	showSelectionSheet: Boolean,
	onSelectionSheetDismiss: () -> Unit,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onSelectionApplied: (PensumOptionItem, PensumModalityItem) -> Unit,
	onPensumContextClick: () -> Unit
) {
	val focusedNodeIdState = remember(model.selection.year, model.selection.modalityId) {
		mutableStateOf<String?>(null)
	}
	val detailNodeIdState = remember(model.selection.year, model.selection.modalityId) {
		mutableStateOf<String?>(null)
	}
	val shouldOpenDetailExpandedState = remember(model.selection.year, model.selection.modalityId) {
		mutableStateOf(false)
	}
	val focusRequestSerialState = remember(model.selection.year, model.selection.modalityId) {
		mutableStateOf(0)
	}
	val nodeIds = remember(model.nodes) {
		model.nodes.mapTo(mutableSetOf()) { node -> node.id }
	}
	val focusedNodeId = focusedNodeIdState.value?.takeIf { nodeId -> nodeId in nodeIds }
	val detailNodeId = detailNodeIdState.value?.takeIf { nodeId -> nodeId in nodeIds }
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
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
			.testTag(PensumUiTags.PensumScreen)
	) {
		PensumSummaryRow(
			model = model,
			onPensumContextClick = {
				clearSubjectContext()
				onPensumContextClick()
			}
		)
		PensumGraphCanvas(
			model = model,
			selectedNodeId = if (showSelectionSheet) null else focusedNodeId,
			onSelectedNodeChange = { nodeId ->
				focusedNodeIdState.value = nodeId
				detailNodeIdState.value = nodeId
				shouldOpenDetailExpandedState.value = false
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
			onSubjectStatsClick = onSubjectStatsClick,
			onRelatedSubjectClick = { nodeId ->
				focusedNodeIdState.value = nodeId
				detailNodeIdState.value = nodeId
				shouldOpenDetailExpandedState.value = true
				focusRequestSerialState.value += 1
			},
			onDismissRequest = {
				detailNodeIdState.value = null
				shouldOpenDetailExpandedState.value = false
			}
		)
	}
}
