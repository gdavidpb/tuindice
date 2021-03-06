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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.dialogs.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.utils.extensions.bottomSheetDialog
import com.gdavidpb.tuindice.utils.extensions.browse
import com.gdavidpb.tuindice.utils.extensions.getCompatColor
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
                    lifecycleScope.launchWhenResumed { sWebView.isRefreshing = true }
                }

                override fun onPageFinished(view: WebView, url: String) {
                    lifecycleScope.launchWhenResumed { sWebView.isRefreshing = false }
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

        val message = getString(R.string.dialog_message_warning_external, externalUrl)

        val spannedMessage = buildSpannedString {
            append(message)

            val startUrl = message.indexOf(externalUrl)
            val endUrl = startUrl + externalUrl.length

            val startHost = message.indexOf(externalHost)
            val endHost = startHost + externalHost.length

            setSpan(ForegroundColorSpan(secondaryColor), startUrl, endUrl, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(primaryColor), startHost, endHost, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        bottomSheetDialog<ConfirmationBottomSheetDialog> {
            titleResource = R.string.dialog_title_warning_external
            messageText = spannedMessage

            positiveButton(R.string.open) { browse(url = externalUrl) }
            negativeButton(R.string.cancel)
        }
    }
}