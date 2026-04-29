package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.subjects.ui.view.SubjectDetailContentView
import com.gdavidpb.tuindice.subjects.ui.view.SubjectDetailLoadingView
import com.gdavidpb.tuindice.subjects.ui.view.SubjectDetailMessageView

@Composable
fun SubjectDetailScreen(
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
	onDismissRequest: () -> Unit,
	scrollEnabled: Boolean = true,
	initialScrollOffset: Dp = 0.dp,
	onChartsVisibilityChange: (Boolean) -> Unit = {}
) {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Box(modifier = Modifier.fillMaxSize()) {
			when (state) {
				SubjectDetail.State.Loading ->
					SubjectDetailLoadingView()

				is SubjectDetail.State.Content ->
					SubjectDetailContentView(
						detail = state.detail,
						selectedTab = state.selectedTab,
						careerTabText = careerTabText,
						globalTabText = globalTabText,
						onTabSelected = onTabSelected,
						scrollEnabled = scrollEnabled,
						initialScrollOffset = initialScrollOffset,
						onChartsVisibilityChange = onChartsVisibilityChange
					)

				is SubjectDetail.State.Unavailable ->
					SubjectDetailMessageView(
						modifier = Modifier.testTag(SubjectsUiTags.Unavailable),
						title = unavailableTitle,
						body = unavailableBody,
						actionText = closeText,
						onActionClick = onDismissRequest
					)

				is SubjectDetail.State.Failed ->
					SubjectDetailMessageView(
						modifier = Modifier.testTag(SubjectsUiTags.Failed),
						title = failedTitle,
						body = state.subjectCode,
						actionText = retryText,
						onActionClick = onRetryClick,
						actionTestTag = SubjectsUiTags.Retry
					)
			}
		}
	}
}
