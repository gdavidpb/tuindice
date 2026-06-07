package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
	val focusRequestSerialState = remember(model.selection.year, model.selection.modalityId) {
		mutableStateOf(0)
	}
	val detailNode = model.nodes.firstOrNull { node -> node.id == detailNodeIdState.value }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
			.testTag(PensumUiTags.PensumScreen)
	) {
		PensumSummaryRow(
			model = model,
			onPensumContextClick = onPensumContextClick
		)
		PensumGraphCanvas(
			model = model,
			selectedNodeId = focusedNodeIdState.value,
				onSelectedNodeChange = { nodeId -> focusedNodeIdState.value = nodeId },
				onNodeDetailClick = { nodeId ->
					focusedNodeIdState.value = nodeId
					detailNodeIdState.value = nodeId
				},
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
				onSubjectStatsClick = onSubjectStatsClick,
				onRelatedSubjectClick = { nodeId ->
					focusedNodeIdState.value = nodeId
					detailNodeIdState.value = nodeId
					focusRequestSerialState.value += 1
				},
				onDismissRequest = { detailNodeIdState.value = null }
			)
	}
}
