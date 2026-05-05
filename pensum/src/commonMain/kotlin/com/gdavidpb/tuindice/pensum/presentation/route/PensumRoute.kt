package com.gdavidpb.tuindice.pensum.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.pensum.ui.screen.PensumScreen

@Composable
fun PensumRoute(
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: PensumViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Pensum.Effect.ShowSnackBar -> showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	LaunchedEffect(Unit) {
		viewModel.refreshPensumAction()
	}

	PensumScreen(
		state = viewState,
		onRetryClick = viewModel::refreshPensumAction,
		onPensumSelected = { item ->
			viewModel.selectPensumAction(
				careerCode = item.careerCode,
				year = item.year
			)
		},
		onModalitySelected = { item ->
			viewModel.selectModalityAction(item.id)
		}
	)
}
