package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.presentation.contract.Browser
import org.koin.compose.koinInject

interface BrowserScreenRenderer {
	@Composable
	fun Render(
		state: Browser.State,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
	)
}

@Composable
fun BrowserScreen(
	state: Browser.State,
	onPageStarted: () -> Unit,
	onPageFinished: () -> Unit,
	onExternalResourceClick: (url: String) -> Unit,
	renderer: BrowserScreenRenderer = koinInject()
) {
	renderer.Render(
		state = state,
		onPageStarted = onPageStarted,
		onPageFinished = onPageFinished,
		onExternalResourceClick = onExternalResourceClick
	)
}
