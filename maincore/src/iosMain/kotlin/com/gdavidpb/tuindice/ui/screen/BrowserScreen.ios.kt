package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSError
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
		url: String,
		modifier: Modifier,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onPageError: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
	) {
		val navigationDelegate = remember(url, onPageStarted, onPageFinished, onPageError, onExternalResourceClick) {
			BrowserNavigationDelegate(
				initialUrl = url,
				onPageStarted = onPageStarted,
				onPageFinished = onPageFinished,
				onPageError = onPageError,
				onExternalResourceClick = onExternalResourceClick
			)
		}

		val webView = remember { WKWebView() }

		DisposableEffect(webView) {
			onDispose {
				webView.stopLoading()
				webView.setNavigationDelegate(null)
			}
		}

		UIKitView(
			factory = { webView },
			update = { browserWebView ->
				browserWebView.setNavigationDelegate(navigationDelegate)

				if (shouldLoadBrowserUrl(currentUrl = browserWebView.URL?.absoluteString, targetUrl = url)) {
					val targetUrl = NSURL.URLWithString(url) ?: return@UIKitView
					val request = NSURLRequest.requestWithURL(targetUrl)
					browserWebView.loadRequest(request)
				}
			},
			modifier = modifier
		)
	}
}

@OptIn(ExperimentalForeignApi::class)
private class BrowserNavigationDelegate(
	private val initialUrl: String,
	private val onPageStarted: () -> Unit,
	private val onPageFinished: () -> Unit,
	private val onPageError: () -> Unit,
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
		didFailNavigation: WKNavigation?,
		withError: NSError
	) {
		onPageError()
	}

	@ObjCSignatureOverride
	override fun webView(
		webView: WKWebView,
		didFailProvisionalNavigation: WKNavigation?,
		withError: NSError
	) {
		onPageError()
	}

	@ObjCSignatureOverride
	override fun webView(
		webView: WKWebView,
		decidePolicyForNavigationAction: WKNavigationAction,
		decisionHandler: (WKNavigationActionPolicy) -> Unit
	) {
		val requestedUrl = decidePolicyForNavigationAction.request.URL?.absoluteString
		val shouldOpenExternal = shouldOpenExternalResource(
			initialUrl = initialUrl,
			currentUrl = webView.URL?.absoluteString,
			requestedUrl = requestedUrl
		)

		if (shouldOpenExternal && requestedUrl != null) {
			onExternalResourceClick(requestedUrl)
			decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
			return
		}

		decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
	}
}
