package com.gdavidpb.tuindice.ui.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.EXTRA_TITLE
import com.gdavidpb.tuindice.utils.EXTRA_URL
import kotlinx.android.synthetic.main.activity_browser.*

class BrowserActivity : AppCompatActivity() {

    private val extraUrl by lazy {
        intent.getStringExtra(EXTRA_URL) ?: error("extraUrl could not be null.")
    }

    private val extraTitle by lazy {
        intent.getStringExtra(EXTRA_TITLE) ?: error("extraTitle could not be null.")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        supportActionBar?.title = extraTitle

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sWebView.isEnabled = false

        with(webView) {
            settings.javaScriptEnabled = true

            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    sWebView.isRefreshing = true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    sWebView.isRefreshing = false
                }
            }

            webView.loadUrl(extraUrl)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }

    override fun onBackPressed() {
        with(webView) {
            if (canGoBack())
                goBack()
            else
                super.onBackPressed()
        }
    }
}