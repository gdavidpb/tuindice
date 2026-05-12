package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.ui.screen.CreateSyntheticTermScreen

@Composable
fun CreateSyntheticTermRoute(
	viewModel: CreateSyntheticTermViewModel,
	onBack: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			CreateSyntheticTerm.Effect.NavigateBack ->
				onBack()

			is CreateSyntheticTerm.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	CreateSyntheticTermScreen(
		state = viewState,
		onQueryChange = viewModel::updateQueryAction,
		onClearQueryClick = viewModel::clearQueryAction,
		onPeriodSelected = viewModel::selectPeriodAction,
		onSubjectAdd = viewModel::addSubjectAction,
		onSubjectRemove = viewModel::removeSubjectAction,
		onCreateClick = viewModel::createTermAction
	)
}
