package com.gdavidpb.tuindice.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import kotlinx.android.synthetic.main.fragment_browser.*

@SuppressLint("SetJavaScriptEnabled")
open class BrowserFragment : NavigationFragment() {
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
            }

            webView.loadUrl(args.url)
        }
    }
}