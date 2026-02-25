package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel

@Composable
fun RecordRoute(
	onNavigateToUpdatePassword: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: RecordViewModel,
	content: @Composable (
		state: Record.State,
		onRetryClick: () -> Unit,
		onSubjectGradeChange: (
			quarterId: String,
			subjectId: String,
			newGrade: Int,
			isSelected: Boolean
		) -> Unit
	) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Record.Effect.NavigateToOutdatedPassword ->
				onNavigateToUpdatePassword()

			is Record.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadQuartersAction()
	}

	content(
		viewState,
		viewModel::loadQuartersAction,
		viewModel::updateSubjectAction
	)
}
