package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.ui.screen.CreateSyntheticTermScreen

@Composable
fun CreateSyntheticTermRoute(
	termId: String?,
	viewModel: CreateSyntheticTermViewModel,
	onBack: () -> Unit,
	onSubjectStatsClick: (String) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(termId) {
		viewModel.configureAction(termId)
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			CreateSyntheticTerm.Effect.NavigateBack ->
				onBack()
		}
	}

	CreateSyntheticTermScreen(
		state = viewState,
		onQueryChange = viewModel::updateQueryAction,
		onClearQueryClick = viewModel::clearQueryAction,
		onPeriodSelected = viewModel::selectPeriodAction,
		onAddSubjectTabSelected = viewModel::selectAddSubjectTabAction,
		onSubjectAdd = viewModel::addSubjectAction,
		onSubjectRemove = viewModel::removeSubjectAction,
		onSubjectStatsClick = onSubjectStatsClick,
		onCreateClick = viewModel::createTermAction
	)
}
