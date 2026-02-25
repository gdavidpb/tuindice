package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosBrowserScreenRenderer : BrowserScreenRenderer {
	@OptIn(ExperimentalForeignApi::class)
	@Composable
	override fun Render(
		state: Browser.State,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
	) {
		if (state !is Browser.State.Content) return

		val navigationDelegate = remember(onPageStarted, onPageFinished, onExternalResourceClick) {
			BrowserNavigationDelegate(
				onPageStarted = onPageStarted,
				onPageFinished = onPageFinished,
				onExternalResourceClick = onExternalResourceClick
			)
		}

		val webView = remember {
			WKWebView().apply {
				setNavigationDelegate(navigationDelegate)
			}
		}

		DisposableEffect(webView) {
			onDispose {
				webView.stopLoading()
				webView.setNavigationDelegate(null)
			}
		}

		androidx.compose.foundation.layout.Column(
			modifier = Modifier.fillMaxSize()
		) {
			if (state.isLoading) {
				LinearProgressIndicator(
					modifier = Modifier.fillMaxWidth()
				)
			}

			UIKitView(
				factory = { webView },
				update = { browserWebView ->
					if (browserWebView.URL?.absoluteString != state.url) {
						val url = NSURL.URLWithString(state.url) ?: return@UIKitView
						val request = NSURLRequest.requestWithURL(url)
						browserWebView.loadRequest(request)
					}
				},
				modifier = Modifier.fillMaxSize()
			)
		}
	}
}

@OptIn(ExperimentalForeignApi::class)
private class BrowserNavigationDelegate(
	private val onPageStarted: () -> Unit,
	private val onPageFinished: () -> Unit,
	private val onExternalResourceClick: (url: String) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
	@ObjCSignatureOverride
	override fun webView(
		webView: WKWebView,
		didStartProvisionalNavigation: WKNavigation?
	) {
		onPageStarted()
	}

	@ObjCSignatureOverride
	override fun webView(
		webView: WKWebView,
		didFinishNavigation: WKNavigation?
	) {
		onPageFinished()
	}

	@ObjCSignatureOverride
	override fun webView(
		webView: WKWebView,
		decidePolicyForNavigationAction: WKNavigationAction,
		decisionHandler: (WKNavigationActionPolicy) -> Unit
	) {
		val url = decidePolicyForNavigationAction.request.URL
		val urlValue = url?.absoluteString

		val shouldOpenExternal = url?.scheme
			?.lowercase()
			?.let { scheme -> scheme != "http" && scheme != "https" }
			?: false

		if (shouldOpenExternal && !urlValue.isNullOrBlank()) {
			onExternalResourceClick(urlValue)
			decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
			return
		}

		decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
	}
}
