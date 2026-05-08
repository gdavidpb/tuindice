package com.gdavidpb.tuindice.subjects.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel
import com.gdavidpb.tuindice.subjects.ui.screen.SubjectSearchScreen

@Composable
fun SubjectSearchRoute(
	viewModel: SubjectSearchViewModel,
	onSubjectClick: (String) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	SubjectSearchScreen(
		state = viewState,
		onQueryChange = viewModel::updateQueryAction,
		onClearClick = { viewModel.updateQueryAction("") },
		onRetryClick = viewModel::retryAction,
		onSubjectClick = onSubjectClick
	)
}
