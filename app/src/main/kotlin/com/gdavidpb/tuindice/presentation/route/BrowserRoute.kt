package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.ui.screen.BrowserScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrowserRoute(
	url: String,
	onNavigateToExternalResourceDialog: (url: String) -> Unit,
	viewModel: BrowserViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Browser.Effect.NavigateToExternalResourceDialog ->
				onNavigateToExternalResourceDialog(effect.url)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.navigateToAction(url = url)
	}

	BrowserScreen(
		state = viewState,
		onPageStarted = viewModel::showLoadingAction,
		onPageFinished = viewModel::hideLoadingAction,
		onExternalResourceClick = viewModel::openExternalResourceAction
	)
}