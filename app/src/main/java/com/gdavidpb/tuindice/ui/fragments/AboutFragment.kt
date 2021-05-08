package com.gdavidpb.tuindice.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
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

        rViewAbout.adapter = aboutAdapter

        val context = requireContext()
        val versionName = requireContext().versionName()
        val version = getString(R.string.about_version, versionName)

        val data = context.about {
            header(R.string.app_name)

            item {
                title(version)
                drawable(
                    drawableRes = R.drawable.ic_version,
                    colorRes = R.color.color_secondary_text
                )
            }

            item {
                title(R.string.about_license)
                drawable(
                    drawableRes = R.drawable.ic_cc,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    browse(url = BuildConfig.URL_CREATIVE_COMMONS)
                }
            }

            item {
                title(R.string.about_terms_and_conditions)
                drawable(
                    drawableRes = R.drawable.ic_terms_and_conditions,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    startBrowser(
                        title = R.string.label_terms_and_conditions,
                        url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
                    )
                }
            }

            item {
                title(R.string.about_privacy_policy)
                drawable(
                    drawableRes = R.drawable.ic_privacy_policy,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    startBrowser(
                        title = R.string.label_privacy_policy,
                        url = BuildConfig.URL_APP_PRIVACY_POLICY
                    )
                }
            }

            item {
                title(R.string.about_twitter)
                drawable(
                    drawableRes = R.drawable.ic_twitter,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    browse(url = BuildConfig.URL_APP_TWITTER)
                }
            }

            item {
                title(R.string.about_share)
                drawable(
                    drawableRes = R.drawable.ic_share,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    share(
                        getString(R.string.about_share_message, context.packageName),
                        getString(R.string.app_name)
                    )
                }
            }

            item {
                title(R.string.about_rate)
                drawable(
                    drawableRes = R.drawable.ic_rate,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    startPlayStore()
                }
            }

            header(R.string.about_header_developer)

            item {
                title(R.string.about_dev_info)
                drawable(
                    drawableRes = R.drawable.ic_profile,
                    colorRes = R.color.color_secondary_text
                )
            }

            item {
                title(R.string.about_source_code)
                drawable(
                    drawableRes = R.drawable.ic_github,
                    colorRes = R.color.color_secondary_text
                )
            }

            item {
                title(R.string.about_dev_contact)
                drawable(
                    drawableRes = R.drawable.ic_contact,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    email(email = contactEmail, subject = contactSubject)
                }
            }

            item {
                title(R.string.about_dev_report)
                drawable(
                    drawableRes = R.drawable.ic_bug_report,
                    colorRes = R.color.color_secondary_text
                )
                onClick {
                    showReportSelector()
                }
            }

            header(R.string.about_header_libs)

            item {
                title(R.string.about_kotlin)
                drawable(drawableRes = R.drawable.ic_kotlin, dimenRes = R.dimen.dp_48)
            }

            item {
                title(R.string.about_firebase)
                drawable(drawableRes = R.drawable.ic_firebase, dimenRes = R.dimen.dp_48)
            }

            item {
                title(R.string.about_koin)
                drawable(drawableRes = R.drawable.ic_koin, dimenRes = R.dimen.dp_48)
            }

            item {
                title(R.string.about_retrofit)
                drawable(drawableRes = R.drawable.ic_square, dimenRes = R.dimen.dp_48)
            }

            header(R.string.about_header_special_thanks)

            item {
                title(R.string.about_dst)
                drawable(drawableRes = R.drawable.ic_usb, dimenRes = R.dimen.dp_48)
            }

            item {
                title(R.string.about_freepik)
                drawable(drawableRes = R.drawable.ic_freepik, dimenRes = R.dimen.dp_48)
            }

            /*
            item {
                title(R.string.about_mit_mapping_lab)
                drawable(R.drawable.ic_mit_mapping_lab, R.dimen.dp_48)
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
