package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.ui.screen.BrowserScreen
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer

@Composable
fun BrowserRoute(
	title: String,
	url: String,
	onNavigateToExternalResourceDialog: (url: String) -> Unit,
	viewModel: BrowserViewModel,
	renderer: BrowserScreenRenderer
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Browser.Effect.NavigateToExternalResourceDialog ->
				onNavigateToExternalResourceDialog(effect.url)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.navigateToAction(
			title = title,
			url = url
		)
	}

	BrowserScreen(
		state = viewState,
		onPageStarted = viewModel::showLoadingAction,
		onPageFinished = viewModel::hideLoadingAction,
		onPageError = viewModel::pageLoadFailedAction,
		onRetryClick = viewModel::retryAction,
		onExternalResourceClick = viewModel::openExternalResourceAction,
		renderer = renderer
	)
}
