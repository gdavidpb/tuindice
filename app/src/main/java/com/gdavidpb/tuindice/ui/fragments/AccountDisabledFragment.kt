package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_account_disabled.*

class AccountDisabledFragment : NavigationFragment() {

    private val contactEmail by config<String>(ConfigKeys.CONTACT_EMAIL)
    private val contactSubject by config<String>(ConfigKeys.CONTACT_SUBJECT)

    override fun onCreateView() = R.layout.fragment_account_disabled

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accentColor = requireContext().getCompatColor(R.color.color_accent)

        val termsAndConditions = getString(R.string.link_terms_and_conditions).toLowerCase(DEFAULT_LOCALE)

        val messageText = getString(R.string.dialog_message_account_disabled, termsAndConditions, contactEmail)

        tViewAccountDisabledMessage.apply {
            text = messageText

            setSpans {
                listOf(ForegroundColorSpan(accentColor), TypefaceSpan("sans-serif-medium"), UnderlineSpan())
            }

            setLink(termsAndConditions) {
                browse(BuildConfig.URL_APP_TERMS_AND_CONDITIONS)
            }

            setLink(contactEmail) {
                email(email = contactEmail, subject = contactSubject)
            }
        }.build()

        btnExit.onClickOnce(::onExitClick)
    }

    private fun onExitClick() {
        requireActivity().finish()
    }
}