package com.gdavidpb.tuindice.base.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri

@SuppressLint("SetJavaScriptEnabled")
fun Context.getWebView(
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
				view: WebView, request: WebResourceRequest
			): Boolean {
				onExternalPageRequested("${request.url}")

				return true
			}
		}
	}
}

fun getAnnotatedUrl(
	url: String,
	primary: Color,
	outlineVariant: Color
): AnnotatedString {
	return buildAnnotatedString {
		runCatching {
			val uri = url.toUri()
			val host = uri.host ?: error("Unable to parse url")

			withStyle(
				style = SpanStyle(
					color = outlineVariant,
					textDecoration = TextDecoration.Underline
				)
			) {
				append(url.substringBefore(host))

				withStyle(
					style = SpanStyle(
						color = primary
					)
				) {
					append(host)
				}

				append(url.substringAfter(host))
			}
		}.getOrElse {
			withStyle(
				style = SpanStyle(
					color = primary,
					textDecoration = TextDecoration.Underline
				)
			) {
				append(url)
			}
		}
	}
}