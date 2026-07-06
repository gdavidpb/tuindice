package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.browser_error_message
import tuindice.maincore.generated.resources.browser_error_retry
import tuindice.maincore.generated.resources.browser_error_title

interface BrowserScreenRenderer {
	@Composable
	fun Render(
		url: String,
		modifier: Modifier,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onPageError: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
	)
}

@Composable
fun BrowserScreen(
	state: Browser.State,
	onPageStarted: () -> Unit,
	onPageFinished: () -> Unit,
	onPageError: () -> Unit,
	onRetryClick: () -> Unit,
	onExternalResourceClick: (url: String) -> Unit,
	renderer: BrowserScreenRenderer
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		if (state !is Browser.State.Content) return@Box

		if (state.hasError) {
			ErrorView(
				title = stringResource(Res.string.browser_error_title),
				message = stringResource(Res.string.browser_error_message),
				retryText = stringResource(Res.string.browser_error_retry),
				onRetryClick = onRetryClick,
				headerContent = {
					ErrorStateAnimationView()
				}
			)
			return@Box
		}

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

			// Keyed on reloadKey so a retry discards the failed platform web view.
			key(state.reloadKey) {
				renderer.Render(
					url = state.url,
					modifier = Modifier.fillMaxSize(),
					onPageStarted = onPageStarted,
					onPageFinished = onPageFinished,
					onPageError = onPageError,
					onExternalResourceClick = onExternalResourceClick
				)
			}
		}
	}
}
