package com.gdavidpb.tuindice.base.presentation.route

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.contract.Browser
import com.gdavidpb.tuindice.base.presentation.model.BrowserDialog
import com.gdavidpb.tuindice.base.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.ui.screen.BrowserScreen
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.browse
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserRoute(
	title: String,
	url: String,
	viewModel: BrowserViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	val context = LocalContext.current
	val sheetState = rememberModalBottomSheetState()
	val dialogState = remember { mutableStateOf<BrowserDialog?>(null) }

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Browser.Event.OpenExternalResourceDialog ->
				context.browse(event.url)

			is Browser.Event.ShowExternalResourceDialog ->
				dialogState.value = BrowserDialog.ExternalResourceDialog(url = event.url)

			is Browser.Event.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.setState(
			Browser.State.Content(
				title = title,
				url = url,
				isLoading = true
			)
		)
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
		onPageStarted = viewModel::showLoading,
		onPageFinished = viewModel::hideLoading,
		onExternalResourceClick = viewModel::openExternalResourceAction
	)
}