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
import com.gdavidpb.tuindice.presentation.model.AboutBase
import com.gdavidpb.tuindice.presentation.model.AboutHeader
import com.gdavidpb.tuindice.presentation.model.AboutLib
import com.gdavidpb.tuindice.ui.adapters.AboutAdapter
import com.gdavidpb.tuindice.utils.URL_CREATIVE_COMMONS
import com.gdavidpb.tuindice.utils.URL_TERMS
import com.gdavidpb.tuindice.utils.getCompatColor
import kotlinx.android.synthetic.main.activity_about.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import org.jetbrains.anko.share

class AboutActivity : AppCompatActivity() {

    private val cachedColors by lazy {
        listOf(
                getCompatColor(R.color.colorPrimaryText),
                getCompatColor(R.color.colorSecondaryText)
        )
    }

    private val adapter = AboutAdapter(callback = AboutManager())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rViewAbout.layoutManager = LinearLayoutManager(this)
        rViewAbout.adapter = adapter

        val versionName = BuildConfig.VERSION_NAME.toFloat()

        val version = getString(R.string.aboutVersion,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                getString(if (versionName < 1.0f) R.string.beta else R.string.alpha))

        val data = listOf<AboutBase>(
                AboutHeader(title = getString(R.string.app_name)),
                About(drawable = R.drawable.ic_version, content = version),
                About(drawable = R.drawable.ic_cc, content = getString(R.string.aboutLicense)) { browse(URL_CREATIVE_COMMONS) },
                About(drawable = R.drawable.ic_terms, content = getString(R.string.aboutTerms)) { browse(URL_TERMS) },
                About(drawable = R.drawable.ic_share, content = getString(R.string.aboutShare)) { share(getString(R.string.aboutShareMessage, packageName), getString(R.string.app_name)) },
                About(drawable = R.drawable.ic_rate, content = getString(R.string.aboutRate)) { startPlayStore() },
                AboutHeader(title = getString(R.string.aboutHeaderDeveloper)),
                About(drawable = R.drawable.ic_profile, content = getString(R.string.aboutDevInfo)),
                About(drawable = R.drawable.ic_github, content = getString(R.string.aboutDevSource)),
                About(drawable = R.drawable.ic_contact, content = getString(R.string.aboutDevContact)) { email(email = getString(R.string.devEmail), subject = getString(R.string.devContactSubject)) },
                AboutHeader(title = getString(R.string.aboutHeadersLibs)),
                AboutLib(drawable = R.drawable.ic_kotlin, content = getString(R.string.aboutKotlin)),
                AboutLib(drawable = R.drawable.ic_firebase, content = getString(R.string.aboutFirebase)),
                AboutLib(drawable = R.drawable.ic_koin, content = getString(R.string.aboutKoin)),
                AboutLib(drawable = R.drawable.ic_square, content = getString(R.string.aboutRetrofit))
        )

        adapter.swapItems(new = data)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }

    private fun startPlayStore() {
        val uri = Uri.parse(getString(R.string.aboutGooglePlayIntent, packageName))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }

        try {
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            browse(getString(R.string.aboutGooglePlay, packageName))
        }
    }

    inner class AboutManager : AboutAdapter.AdapterCallback {
        override fun resolveColors(): List<Int> {
            return cachedColors
        }
    }
}
