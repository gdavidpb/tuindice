package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
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
	loadingTitle: String,
	loadingMessage: String,
	unavailableTitle: String,
	unavailableBody: String,
	failedTitle: String,
	failedMessage: String,
	retryText: String,
	closeText: String,
	onRetryClick: () -> Unit,
	onTabSelected: (SubjectSegmentTab) -> Unit,
	onDismissRequest: () -> Unit,
	scrollEnabled: Boolean = true,
	initialScrollOffset: Dp = 0.dp,
	onChartsVisibilityChange: (Boolean) -> Unit = {}
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		when (state) {
			SubjectDetail.State.Idle -> Unit

			SubjectDetail.State.Loading ->
				SubjectDetailLoadingView(
					title = loadingTitle,
					message = loadingMessage
				)

			is SubjectDetail.State.Content ->
				SubjectDetailContentView(
					detail = state.detail,
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
					onActionClick = onDismissRequest,
					headerContent = { EmptyStateAnimationView() }
				)

			is SubjectDetail.State.Failed ->
				SubjectDetailMessageView(
					modifier = Modifier.testTag(SubjectsUiTags.Failed),
					title = failedTitle,
					body = failedMessage,
					actionText = retryText,
					onActionClick = onRetryClick,
					actionTestTag = SubjectsUiTags.Retry,
					headerContent = { ErrorStateAnimationView() }
				)
		}
	}
}
