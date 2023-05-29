package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SummaryRoute(
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: SummaryViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Summary.Event.OpenCamera ->
				TODO()

			is Summary.Event.OpenPicker ->
				TODO()

			is Summary.Event.ShowSnackBar ->
				showSnackBar(event.message, null, null)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadSummaryAction()
	}

	SummaryScreen(
		state = viewState,
		onRetryClick = viewModel::loadSummaryAction,
		onEditProfilePictureClick = { }
	)
}