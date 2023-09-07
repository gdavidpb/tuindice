package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.ui.screen.RecordScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecordRoute(
	onNavigateToUpdatePassword: () -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: RecordViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Record.Event.NavigateToOutdatedPassword ->
				onNavigateToUpdatePassword()

			is Record.Event.ShowSnackBar ->
				showSnackBar(
					event.message,
					null,
					null
				)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadQuartersAction()
	}

	RecordScreen(
		state = viewState,
		onRetryClick = viewModel::loadQuartersAction,
		onSubjectGradeChanged = { subjectId, newGrade, isSelected ->
			viewModel.updateSubjectAction(
				UpdateSubjectParams(
					subjectId = subjectId,
					grade = newGrade,
					dispatchToRemote = isSelected
				)
			)
		}
	)
}