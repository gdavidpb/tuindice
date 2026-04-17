package com.gdavidpb.tuindice.subjects.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.subjects.ui.screen.SubjectDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailDialog(
	state: SubjectDetail.State,
	careerTabText: String,
	globalTabText: String,
	unavailableTitle: String,
	unavailableBody: String,
	failedTitle: String,
	retryText: String,
	closeText: String,
	onRetryClick: () -> Unit,
	onTabSelected: (SubjectSegmentTab) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ModalBottomSheet(
		modifier = Modifier.testTag(SubjectsUiTags.Sheet),
		onDismissRequest = onDismissRequest,
		sheetState = sheetState
	) {
		SubjectDetailScreen(
			state = state,
			careerTabText = careerTabText,
			globalTabText = globalTabText,
			unavailableTitle = unavailableTitle,
			unavailableBody = unavailableBody,
			failedTitle = failedTitle,
			retryText = retryText,
			closeText = closeText,
			onRetryClick = onRetryClick,
			onTabSelected = onTabSelected,
			onDismissRequest = onDismissRequest
		)
	}
}
