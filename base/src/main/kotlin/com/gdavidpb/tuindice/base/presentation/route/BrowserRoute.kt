package com.gdavidpb.tuindice.base.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.contract.Browser
import com.gdavidpb.tuindice.base.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.base.ui.screen.BrowserScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrowserRoute(
	title: String,
	url: String,
	viewModel: BrowserViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	LaunchedEffect(Unit) {
		viewModel.setState(
			Browser.State.Content(
				title = title,
				url = url,
				isLoading = true
			)
		)
	}

	BrowserScreen(
		state = viewState,
		onPageStarted = viewModel::showLoading,
		onPageFinished = viewModel::hideLoading,
		onDismissRequest = viewModel::closeExternalResourceDialogAction,
		onExternalResourceClick = viewModel::clickExternalResourceAction
	)
}