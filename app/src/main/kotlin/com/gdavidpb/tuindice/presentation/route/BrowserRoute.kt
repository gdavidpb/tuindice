package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.model.BrowserDialog
import com.gdavidpb.tuindice.base.presentation.model.rememberDialogState
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.ui.screen.BrowserScreen
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.browse
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserRoute(
	url: String,
	viewModel: com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	val context = LocalContext.current
	val sheetState = rememberModalBottomSheetState()
	val dialogState = rememberDialogState<BrowserDialog>()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Browser.Effect.OpenExternalResource ->
				context.browse(effect.url)

			is Browser.Effect.ShowExternalResourceDialog ->
				dialogState.value = BrowserDialog.ExternalResourceDialog(url = effect.url)

			is Browser.Effect.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.navigateToAction( url = url)
	}

	when (val state = dialogState.value) {
		is BrowserDialog.ExternalResourceDialog ->
			ExternalResourceDialog(
				sheetState = sheetState,
				url = state.url,
				onConfirmClick = viewModel::confirmOpenExternalResourceAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		null -> {}
	}

	BrowserScreen(
		state = viewState,
		onPageStarted = viewModel::showLoadingAction,
		onPageFinished = viewModel::hideLoadingAction,
		onExternalResourceClick = viewModel::openExternalResourceAction
	)
}