package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.presentation.contract.Browser
import org.koin.compose.koinInject

interface BrowserScreenRenderer {
	@Composable
	fun Render(
		url: String,
		modifier: Modifier,
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
	if (state !is Browser.State.Content) return

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		if (state.isLoading) {
			LinearProgressIndicator(
				modifier = Modifier.fillMaxWidth()
			)
		}

		renderer.Render(
			url = state.url,
			modifier = Modifier.fillMaxSize(),
			onPageStarted = onPageStarted,
			onPageFinished = onPageFinished,
			onExternalResourceClick = onExternalResourceClick
		)
	}
}
