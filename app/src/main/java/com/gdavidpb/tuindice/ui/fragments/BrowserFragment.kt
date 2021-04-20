package com.gdavidpb.tuindice.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.text.buildSpannedString
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_browser.*

@SuppressLint("SetJavaScriptEnabled")
class BrowserFragment : NavigationFragment() {
    private val args by navArgs<BrowserFragmentArgs>()

    override fun onCreateView() = R.layout.fragment_browser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sWebView.isEnabled = false

        with(webView) {
            settings.javaScriptEnabled = true

            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    sWebView.isRefreshing = true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    sWebView.isRefreshing = false
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    showExternalResourceDialog(url = request.url)

                    return true
                }
            }

            loadUrl(args.url)
        }
    }

    private fun showExternalResourceDialog(url: Uri) {
        val externalUrl = "$url"
        val externalHost = url.host ?: externalUrl

        val primaryColor = requireContext().getCompatColor(R.color.color_primary_text)
        val secondaryColor = requireContext().getCompatColor(R.color.color_disabled_text)

        val message = getString(R.string.alert_message_warning_external, externalUrl)

        val spannedMessage = buildSpannedString {
            append(message)

            val startUrl = message.indexOf(externalUrl)
            val endUrl = startUrl + externalUrl.length

            val startHost = message.indexOf(externalHost)
            val endHost = startHost + externalHost.length

            setSpan(ForegroundColorSpan(secondaryColor), startUrl, endUrl, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(primaryColor), startHost, endHost, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        alert {
            titleResource = R.string.alert_title_warning_external

            setMessage(spannedMessage)

            positiveButton(R.string.yes) {
                browse(url = externalUrl)
            }

            negativeButton(R.string.cancel)
        }
    }
}