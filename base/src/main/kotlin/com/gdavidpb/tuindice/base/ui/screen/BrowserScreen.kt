package com.gdavidpb.tuindice.base.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gdavidpb.tuindice.base.presentation.contract.Browser
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.ui.view.getWebView
import com.gdavidpb.tuindice.base.utils.extension.browse

@Composable
fun BrowserScreen(
	state: Browser.State,
	onPageStarted: () -> Unit,
	onPageFinished: () -> Unit,
	onDismissRequest: () -> Unit,
	onExternalResourceClick: (url: String) -> Unit
) {
	val context = LocalContext.current

	val webView = remember {
		context.getWebView(
			onPageStarted = onPageStarted,
			onPageFinished = onPageFinished,
			onExternalPageRequested = onExternalResourceClick
		)
	}

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		when (state) {
			is Browser.State.Idle -> {}
			is Browser.State.Content -> {
				if (state.isLoading)
					LinearProgressIndicator(
						modifier = Modifier.fillMaxWidth()
					)

				if (state.isShowExternalResourceDialog)
					ExternalResourceDialog(
						url = state.externalResourceUrl,
						onConfirmClick = { context.browse(state.externalResourceUrl) },
						onCancelClick = onDismissRequest,
						onDismissRequest = onDismissRequest
					)

				AndroidView(
					factory = { webView },
					update = { webView -> if (webView.url != state.url) webView.loadUrl(state.url) },
					modifier = Modifier.fillMaxSize()
				)
			}
		}
	}
}