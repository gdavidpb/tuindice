package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.ui.MaincoreUiTags

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
	renderer: BrowserScreenRenderer
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		if (state !is Browser.State.Content) return@Box

		Column(
			modifier = Modifier
				.testTag(MaincoreUiTags.BrowserContainer)
				.fillMaxSize()
		) {
			if (state.isLoading) {
				LinearProgressIndicator(
					modifier = Modifier
						.testTag(MaincoreUiTags.BrowserLoadingIndicator)
						.fillMaxWidth()
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
}
