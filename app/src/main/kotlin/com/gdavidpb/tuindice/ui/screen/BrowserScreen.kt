package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gdavidpb.tuindice.base.ui.view.getWebView
import com.gdavidpb.tuindice.presentation.contract.Browser

@Composable
fun BrowserScreen(
	state: Browser.State,
	onPageStarted: () -> Unit,
	onPageFinished: () -> Unit,
	onExternalResourceClick: (url: String) -> Unit
) {
	if (state !is Browser.State.Content) return

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
		if (state.isLoading)
			LinearProgressIndicator(
				modifier = Modifier.fillMaxWidth()
			)

		AndroidView(
			factory = { webView },
			update = { webView -> if (webView.url != state.url) webView.loadUrl(state.url) },
			modifier = Modifier.fillMaxSize()
		)
	}
}