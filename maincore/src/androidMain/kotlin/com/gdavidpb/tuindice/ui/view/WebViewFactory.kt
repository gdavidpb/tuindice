package com.gdavidpb.tuindice.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
fun Context.createBrowserWebView(
	onPageStarted: () -> Unit,
	onPageFinished: () -> Unit,
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

			override fun shouldOverrideUrlLoading(
				view: WebView,
				request: WebResourceRequest
			): Boolean {
				onExternalPageRequested("${request.url}")
				return true
			}
		}
	}
}
