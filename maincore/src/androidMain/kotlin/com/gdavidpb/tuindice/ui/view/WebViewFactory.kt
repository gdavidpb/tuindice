package com.gdavidpb.tuindice.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.gdavidpb.tuindice.ui.screen.shouldOpenExternalResource

@SuppressLint("SetJavaScriptEnabled")
fun Context.createBrowserWebView(
	initialUrl: String,
	onPageStarted: () -> Unit,
	onPageFinished: () -> Unit,
	onPageError: () -> Unit,
	onExternalPageRequested: (url: String) -> Unit
): WebView {
	return WebView(this).apply {
		settings.javaScriptEnabled = true
		webChromeClient = WebChromeClient()

		webViewClient = object : WebViewClient() {
			override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
				onPageStarted()
			}

			override fun onPageFinished(view: WebView, url: String) {
				onPageFinished()
			}

			override fun onReceivedError(
				view: WebView,
				request: WebResourceRequest,
				error: WebResourceError
			) {
				if (request.isForMainFrame) {
					onPageError()
				}
			}

			override fun shouldOverrideUrlLoading(
				view: WebView,
				request: WebResourceRequest
			): Boolean {
				val requestedUrl = "${request.url}"
				val shouldOpenExternal = shouldOpenExternalResource(
					initialUrl = initialUrl,
					currentUrl = view.url,
					requestedUrl = requestedUrl
				)

				if (shouldOpenExternal) {
					onExternalPageRequested(requestedUrl)
				}

				return shouldOpenExternal
			}
		}
	}
}
