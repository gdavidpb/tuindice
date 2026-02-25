package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gdavidpb.tuindice.ui.view.createBrowserWebView

class AndroidBrowserScreenRenderer : BrowserScreenRenderer {
	@Composable
	override fun Render(
		url: String,
		modifier: Modifier,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
	) {
		val context = LocalContext.current
		val webView = remember(context) {
			context.createBrowserWebView(
				onPageStarted = onPageStarted,
				onPageFinished = onPageFinished,
				onExternalPageRequested = onExternalResourceClick
			)
		}

		DisposableEffect(webView) {
			onDispose {
				webView.stopLoading()
				webView.destroy()
			}
		}

		AndroidView(
			factory = { webView },
			update = { browserWebView ->
				if (browserWebView.url != url) {
					browserWebView.loadUrl(url)
				}
			},
			modifier = modifier
		)
	}
}
