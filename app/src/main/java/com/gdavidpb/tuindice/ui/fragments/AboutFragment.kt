package com.gdavidpb.tuindice.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.About
import com.gdavidpb.tuindice.presentation.model.AboutHeader
import com.gdavidpb.tuindice.presentation.model.AboutLib
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.adapters.AboutAdapter
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.browserActivity
import com.gdavidpb.tuindice.utils.extensions.getCompatColor
import kotlinx.android.synthetic.main.fragment_about.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import org.jetbrains.anko.share
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class AboutFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    private val cachedColors by lazy {
        listOf(
                requireContext().getCompatColor(R.color.color_primary_text),
                requireContext().getCompatColor(R.color.color_secondary_text)
        )
    }

    private val adapter = AboutAdapter(manager = AboutManager())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(false)

        val context = requireContext()

        rViewAbout.layoutManager = LinearLayoutManager(context)
        rViewAbout.adapter = adapter

        val versionName = BuildConfig.VERSION_NAME.toFloat()

        val version = getString(R.string.about_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                getString(if (versionName < 1.0f) R.string.beta else R.string.release))

        val data = listOf(
                AboutHeader(title = getString(R.string.app_name)),
                About(drawable = R.drawable.ic_version, content = version),
                About(drawable = R.drawable.ic_cc, content = getString(R.string.about_license)) { context.browserActivity(title = R.string.label_creative_commons, url = URL_CREATIVE_COMMONS) },
                About(drawable = R.drawable.ic_privacy_policy, content = getString(R.string.about_privacy_policy)) { context.browserActivity(title = R.string.label_privacy_policy, url = URL_PRIVACY_POLICY) },
                About(drawable = R.drawable.ic_twitter, content = getString(R.string.about_twitter)) { context.browse(URL_TWITTER) },
                About(drawable = R.drawable.ic_share, content = getString(R.string.about_share)) { context.share(getString(R.string.about_share_message, context.packageName), getString(R.string.app_name)) },
                About(drawable = R.drawable.ic_rate, content = getString(R.string.about_rate)) { startPlayStore() },
                AboutHeader(title = getString(R.string.about_header_developer)),
                About(drawable = R.drawable.ic_profile, content = getString(R.string.about_dev_info)),
                About(drawable = R.drawable.ic_github, content = getString(R.string.about_source_code)),
                About(drawable = R.drawable.ic_contact, content = getString(R.string.about_dev_contact)) { context.email(email = EMAIL_CONTACT, subject = EMAIL_SUBJECT_CONTACT) },
                About(drawable = R.drawable.ic_report, content = getString(R.string.about_dev_report)) { showReportSelector() },
                AboutHeader(title = getString(R.string.about_header_libs)),
                AboutLib(drawable = R.drawable.ic_kotlin, content = getString(R.string.about_kotlin)),
                AboutLib(drawable = R.drawable.ic_firebase, content = getString(R.string.about_firebase)),
                AboutLib(drawable = R.drawable.ic_koin, content = getString(R.string.about_koin)),
                AboutLib(drawable = R.drawable.ic_square, content = getString(R.string.about_retrofit)),
                AboutLib(drawable = R.drawable.ic_freepik, content = getString(R.string.about_freepik)),
                AboutHeader(title = getString(R.string.about_header_settings)),
                About(drawable = R.drawable.ic_sign_out, content = getString(R.string.about_sign_out)) { showSignOutDialog() }
        )

        adapter.swapItems(new = data)
    }

    private fun startPlayStore() {
        val context = requireContext()
        val uri = Uri.parse(getString(R.string.about_google_play_intent, context.packageName))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }

        runCatching {
            startActivity(intent)
        }.onFailure {
            context.browse(url = getString(R.string.about_google_play, context.packageName))
        }
    }

    private fun showReportSelector() {
        val title = getString(R.string.selector_title_report)
        val items = resources.getStringArray(R.array.selector_report_items).toList()

        selector(title, items) { _, index ->
            val selected = items[index]

            requireContext().email(email = EMAIL_CONTACT, subject = selected)
        }
    }

    private fun showSignOutDialog() {
        alert {
            titleResource = R.string.alert_title_sign_out
            messageResource = R.string.alert_message_sign_out

            positiveButton(R.string.yes) {
                viewModel.signOut()
            }

            negativeButton(R.string.cancel) { }
        }.show()
    }

    inner class AboutManager : AboutAdapter.AdapterManager {
        override fun resolveColors(): List<Int> {
            return cachedColors
        }
    }
}
