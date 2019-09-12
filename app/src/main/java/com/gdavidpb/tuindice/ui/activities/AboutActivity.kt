package com.gdavidpb.tuindice.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.About
import com.gdavidpb.tuindice.presentation.model.AboutHeader
import com.gdavidpb.tuindice.presentation.model.AboutLib
import com.gdavidpb.tuindice.ui.adapters.AboutAdapter
import com.gdavidpb.tuindice.utils.*
import kotlinx.android.synthetic.main.activity_about.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import org.jetbrains.anko.share

class AboutActivity : AppCompatActivity() {

    private val cachedColors by lazy {
        listOf(
                getCompatColor(R.color.color_primary_text),
                getCompatColor(R.color.color_secondary_text)
        )
    }

    private val adapter = AboutAdapter(manager = AboutManager())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rViewAbout.layoutManager = LinearLayoutManager(this)
        rViewAbout.adapter = adapter

        val versionName = BuildConfig.VERSION_NAME.toFloat()

        val version = getString(R.string.about_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                getString(if (versionName < 1.0f) R.string.beta else R.string.alpha))

        val data = listOf(
                AboutHeader(title = getString(R.string.app_name)),
                About(drawable = R.drawable.ic_version, content = version),
                About(drawable = R.drawable.ic_cc, content = getString(R.string.about_license)) { browserActivity(title = R.string.label_creative_commons, url = URL_CREATIVE_COMMONS) },
                About(drawable = R.drawable.ic_privacy_policy, content = getString(R.string.about_privacy_policy)) { browserActivity(title = R.string.label_privacy_policy, url = URL_PRIVACY_POLICY) },
                About(drawable = R.drawable.ic_share, content = getString(R.string.about_share)) { share(getString(R.string.about_share_message, packageName), getString(R.string.app_name)) },
                About(drawable = R.drawable.ic_rate, content = getString(R.string.about_rate)) { startPlayStore() },
                AboutHeader(title = getString(R.string.about_header_developer)),
                About(drawable = R.drawable.ic_profile, content = getString(R.string.about_dev_info)),
                About(drawable = R.drawable.ic_github, content = getString(R.string.about_source_code)),
                About(drawable = R.drawable.ic_contact, content = getString(R.string.about_dev_contact)) { email(email = EMAIL_CONTACT, subject = EMAIL_SUBJECT_CONTACT) },
                AboutHeader(title = getString(R.string.about_headers_libs)),
                AboutLib(drawable = R.drawable.ic_kotlin, content = getString(R.string.about_kotlin)),
                AboutLib(drawable = R.drawable.ic_firebase, content = getString(R.string.about_firebase)),
                AboutLib(drawable = R.drawable.ic_koin, content = getString(R.string.about_koin)),
                AboutLib(drawable = R.drawable.ic_square, content = getString(R.string.about_retrofit)),
                AboutLib(drawable = R.drawable.ic_freepik, content = getString(R.string.about_freepik))
        )

        adapter.swapItems(new = data)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }

    private fun startPlayStore() {
        val uri = Uri.parse(getString(R.string.about_google_play_intent, packageName))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }

        try {
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            browse(url = getString(R.string.about_google_play, packageName))
        }
    }

    inner class AboutManager : AboutAdapter.AdapterManager {
        override fun resolveColors(): List<Int> {
            return cachedColors
        }
    }
}
