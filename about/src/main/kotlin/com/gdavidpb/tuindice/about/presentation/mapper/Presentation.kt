package com.gdavidpb.tuindice.about.presentation.mapper

import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.base.utils.extension.append

fun String.spanAbout(titleColor: Int, subtitleColor: Int): CharSequence {
	val (title, subtitle) = listOf(
		substringBefore('\n'),
		substringAfter('\n')
	)

	return buildSpannedString {
		append(title, ForegroundColorSpan(titleColor))
		append('\n')
		append(subtitle, ForegroundColorSpan(subtitleColor), TypefaceSpan("sans-serif-light"))
	}
}