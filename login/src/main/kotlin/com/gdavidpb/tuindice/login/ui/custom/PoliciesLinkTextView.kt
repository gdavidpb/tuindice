package com.gdavidpb.tuindice.login.ui.custom

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import com.gdavidpb.tuindice.base.utils.extension.getCompatColor
import com.gdavidpb.tuindice.login.R

class PoliciesLinkTextView(context: Context, attrs: AttributeSet) : LinkTextView(context, attrs) {
	private var termsAndConditionsClickListener: (() -> Unit)? = null
	private var privacyPolicyClickListener: (() -> Unit)? = null

	fun onTermsAndConditionsClick(listener: () -> Unit) {
		termsAndConditionsClickListener = listener
	}

	fun onPrivacyPolicyClick(listener: () -> Unit) {
		privacyPolicyClickListener = listener
	}

	init {
		val accentColor = context.getCompatColor(R.color.color_accent)

		setSpans {
			listOf(
				ForegroundColorSpan(accentColor),
				TypefaceSpan("sans-serif-medium"),
				UnderlineSpan()
			)
		}

		setLink(context.getString(R.string.link_terms_and_conditions)) {
			termsAndConditionsClickListener?.invoke()
		}

		setLink(context.getString(R.string.link_privacy_policy)) {
			privacyPolicyClickListener?.invoke()
		}

		build()
	}
}