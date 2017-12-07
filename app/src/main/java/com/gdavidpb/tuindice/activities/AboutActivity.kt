package com.gdavidpb.tuindice.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.gdavidpb.tuindice.abstracts.Initializer
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_about.*
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.content.res.AppCompatResources
import com.gdavidpb.tuindice.*

class AboutActivity : AppCompatActivity(), Initializer {

    override fun onInitialize(view: View?) {
        /* Set up content */
        val versionName = BuildConfig.VERSION_NAME.toFloat()

        tViewAboutVersion.text = getString(R.string.aboutVersion,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                getString(if (versionName < 1.0f) R.string.beta else R.string.alpha))

        /* Spannable TextView */
        val textViewArray = arrayOf<TextView>(
                tViewLibJSoup,
                tViewLibKotlin,
                tViewAboutRate,
                tViewDevSource,
                tViewLibFabric,
                tViewAboutShare,
                tViewAboutLicense,
                tViewAboutVersion,
                tViewAboutTerms,
                tViewDevInfo,
                tViewDevContact)

        val titleForegroundColor = ContextCompat
                .getColor(this, R.color.colorPrimaryText)

        val subtitleForegroundColor = ContextCompat
                .getColor(this, R.color.colorSecondaryText)

        for (textView in textViewArray) {
            val content = textView.text.toString()
            val spannableString = SpannableString(content)

            spannableString.setSpan(ForegroundColorSpan(titleForegroundColor),
                    0, content.indexOf('\n'),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString.setSpan(ForegroundColorSpan(subtitleForegroundColor),
                    content.indexOf('\n'), content.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            textView.text = spannableString
        }

        if (versionName < 1.0)
            tViewAboutRate.visibility = View.GONE

        tViewAboutShare.setOnClickListener { launchShare() }
        tViewAboutRate.setOnClickListener { launchPlayStore() }
        tViewDevContact.setOnClickListener { onContact(false) }
        tViewAboutLicense.setOnClickListener { navigateTo(Constants.CREATIVE_COMMONS_LICENSE) }
        tViewAboutTerms.setOnClickListener { alertDialog {
            setTitle(R.string.alertTitleTerms)
            setMessage(R.string.alertMessageTerms)
            setPositiveButton(R.string.close, null)
        } }

        /* Set up drawables */
        val drawableLauncher = AppCompatResources.getDrawable(this, R.drawable.ic_launcher)
        val drawableVersion = AppCompatResources.getDrawable(this, R.drawable.ic_version)
        val drawableLicense = AppCompatResources.getDrawable(this, R.drawable.ic_cc)
        val drawableTerms = AppCompatResources.getDrawable(this, R.drawable.ic_terms)
        val drawableShare = AppCompatResources.getDrawable(this, R.drawable.ic_share)
        val drawableRate = AppCompatResources.getDrawable(this, R.drawable.ic_rate)
        val drawableProfile = AppCompatResources.getDrawable(this, R.drawable.ic_profile)
        val drawableContact = AppCompatResources.getDrawable(this, R.drawable.ic_contact)
        val drawableJsoup = AppCompatResources.getDrawable(this, R.drawable.ic_jsoup)
        val drawableFabric = AppCompatResources.getDrawable(this, R.drawable.ic_fabric)
        val drawableGitHub = AppCompatResources.getDrawable(this, R.drawable.ic_github)

        tViewDevSource.setCompoundDrawablesWithIntrinsicBounds(drawableGitHub, null, null, null)
        tViewLibJSoup.setCompoundDrawablesWithIntrinsicBounds(drawableJsoup, null, null, null)
        tViewLibFabric.setCompoundDrawablesWithIntrinsicBounds(drawableFabric, null, null, null)
        tViewDevContact.setCompoundDrawablesWithIntrinsicBounds(drawableContact, null, null, null)
        tViewDevInfo.setCompoundDrawablesWithIntrinsicBounds(drawableProfile, null, null, null)
        tViewAboutRate.setCompoundDrawablesWithIntrinsicBounds(drawableRate, null, null, null)
        tViewAboutTerms.setCompoundDrawablesWithIntrinsicBounds(drawableTerms, null, null, null)
        tViewAboutShare.setCompoundDrawablesWithIntrinsicBounds(drawableShare, null, null, null)
        tViewAboutLicense.setCompoundDrawablesWithIntrinsicBounds(drawableLicense, null, null, null)
        tViewAboutVersion.setCompoundDrawablesWithIntrinsicBounds(drawableVersion, null, null, null)
        tViewAbout.setCompoundDrawablesWithIntrinsicBounds(drawableLauncher, null, null, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Set up activity */
        setContentView(R.layout.activity_about)

        onInitialize()
    }

    private fun navigateTo(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        startActivity(intent)
    }

    private fun launchPlayStore() {
        val uri = Uri.parse(String.format(Constants.GOOGLE_PLAY_INTENT, packageName))
        val intent = Intent(Intent.ACTION_VIEW, uri)

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)

        try {
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            navigateTo(String.format(Constants.GOOGLE_PLAY_URL, packageName))
        }

    }

    private fun launchShare() {
        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "text/plain"

        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.aboutShareMessage, packageName))

        val chooser = Intent.createChooser(intent, getString(R.string.share))

        startActivity(chooser)
    }
}
