package com.gdavidpb.tuindice.subjects.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.subjects.ui.screen.SubjectDetailScreen
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_action_close
import tuindice.subjects.generated.resources.subjects_message_no_data
import tuindice.subjects.generated.resources.subjects_message_no_data_body
import tuindice.subjects.generated.resources.subjects_message_retry
import tuindice.subjects.generated.resources.subjects_title_failed
import tuindice.subjects.generated.resources.subjects_title_general
import tuindice.subjects.generated.resources.subjects_title_my_career

@Composable
fun SubjectDetailRoute(
	subjectCode: String,
	viewModel: SubjectDetailViewModel,
	onDismissRequest: () -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(subjectCode) {
		viewModel.loadSubjectDetailAction(subjectCode)
	}

	SubjectDetailScreen(
		state = viewState,
		careerTabText = stringResource(Res.string.subjects_title_my_career),
		globalTabText = stringResource(Res.string.subjects_title_general),
		unavailableTitle = stringResource(Res.string.subjects_message_no_data),
		unavailableBody = stringResource(Res.string.subjects_message_no_data_body),
		failedTitle = stringResource(Res.string.subjects_title_failed),
		retryText = stringResource(Res.string.subjects_message_retry),
		closeText = stringResource(Res.string.subjects_action_close),
		onRetryClick = { viewModel.refreshSubjectDetailAction(subjectCode) },
		onTabSelected = viewModel::selectSubjectSegmentTabAction,
		onDismissRequest = onDismissRequest
	)
}
