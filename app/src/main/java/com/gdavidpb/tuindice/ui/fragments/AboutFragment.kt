package com.gdavidpb.tuindice.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.adapters.AboutAdapter
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : NavigationFragment() {

    private val aboutAdapter = AboutAdapter()

    private val contactEmail by config<String>(ConfigKeys.CONTACT_EMAIL)
    private val contactSubject by config<String>(ConfigKeys.CONTACT_SUBJECT)
    private val issuesList by config<List<String>>(ConfigKeys.ISSUES_LIST)

    override fun onCreateView() = R.layout.fragment_about

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        with(rViewAbout) {
            layoutManager = LinearLayoutManager(context)
            adapter = aboutAdapter
        }

        val context = requireContext()
        val versionName = requireContext().versionName()
        val version = getString(R.string.about_version, versionName)

        val data = context.about {
            header(R.string.app_name)

            item {
                title(version)
                tintedDrawable(R.drawable.ic_version, R.color.color_secondary_text)
            }

            item {
                title(R.string.about_license)
                tintedDrawable(R.drawable.ic_cc, R.color.color_secondary_text)
                onClick {
                    browse(url = BuildConfig.URL_CREATIVE_COMMONS)
                }
            }

            item {
                title(R.string.about_terms_and_conditions)
                tintedDrawable(R.drawable.ic_terms_and_conditions, R.color.color_secondary_text)
                onClick {
                    startBrowser(title = R.string.label_terms_and_conditions, url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS)
                }
            }

            item {
                title(R.string.about_privacy_policy)
                tintedDrawable(R.drawable.ic_privacy_policy, R.color.color_secondary_text)
                onClick {
                    startBrowser(title = R.string.label_privacy_policy, url = BuildConfig.URL_APP_PRIVACY_POLICY)
                }
            }

            item {
                title(R.string.about_twitter)
                tintedDrawable(R.drawable.ic_twitter, R.color.color_secondary_text)
                onClick {
                    browse(url = BuildConfig.URL_APP_TWITTER)
                }
            }

            item {
                title(R.string.about_share)
                tintedDrawable(R.drawable.ic_share, R.color.color_secondary_text)
                onClick {
                    share(getString(R.string.about_share_message, context.packageName), getString(R.string.app_name))
                }
            }

            item {
                title(R.string.about_rate)
                tintedDrawable(R.drawable.ic_rate, R.color.color_secondary_text)
                onClick {
                    startPlayStore()
                }
            }

            header(R.string.about_header_developer)

            item {
                title(R.string.about_dev_info)
                tintedDrawable(R.drawable.ic_profile, R.color.color_secondary_text)
            }

            item {
                title(R.string.about_source_code)
                tintedDrawable(R.drawable.ic_github, R.color.color_secondary_text)
            }

            item {
                title(R.string.about_dev_contact)
                tintedDrawable(R.drawable.ic_contact, R.color.color_secondary_text)
                onClick {
                    email(email = contactEmail, subject = contactSubject)
                }
            }

            item {
                title(R.string.about_dev_report)
                tintedDrawable(R.drawable.ic_bug_report, R.color.color_secondary_text)
                onClick {
                    showReportSelector()
                }
            }

            header(R.string.about_header_libs)

            item {
                title(R.string.about_kotlin)
                sizedDrawable(R.drawable.ic_kotlin, R.dimen.dp_48)
            }

            item {
                title(R.string.about_firebase)
                sizedDrawable(R.drawable.ic_firebase, R.dimen.dp_48)
            }

            item {
                title(R.string.about_koin)
                sizedDrawable(R.drawable.ic_koin, R.dimen.dp_48)
            }

            item {
                title(R.string.about_retrofit)
                sizedDrawable(R.drawable.ic_square, R.dimen.dp_48)
            }

            header(R.string.about_header_special_thanks)

            item {
                title(R.string.about_dst)
                sizedDrawable(R.drawable.ic_usb, R.dimen.dp_48)
            }

            item {
                title(R.string.about_freepik)
                sizedDrawable(R.drawable.ic_freepik, R.dimen.dp_48)
            }

            /*
            item {
                title(R.string.about_mit_mapping_lab)
                sizedDrawable(R.drawable.ic_mit_mapping_lab, R.dimen.dp_48)
            }
            */
        }.build()

        aboutAdapter.submitAbout(data)
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
            browse(url = getString(R.string.about_google_play, context.packageName))
        }
    }

    private fun startBrowser(@StringRes title: Int, url: String) {
        runCatching {
            navigate(AboutFragmentDirections.navToUrl(
                    title = getString(title),
                    url = url
            ))
        }.onFailure {
            browse(url)
        }
    }

    private fun showReportSelector() {
        val items = issuesList.toTypedArray()

        selector(R.string.selector_title_report, items) { selected ->
            email(email = contactEmail, subject = selected)
        }
    }
}
