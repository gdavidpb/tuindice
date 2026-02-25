package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel

@Composable
fun BrowserRoute(
	title: String,
	url: String,
	onNavigateToExternalResourceDialog: (url: String) -> Unit,
	content: @Composable (
		state: Browser.State,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
	) -> Unit,
	viewModel: BrowserViewModel
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

	content(
		viewState,
		viewModel::showLoadingAction,
		viewModel::hideLoadingAction,
		viewModel::openExternalResourceAction
	)
}
