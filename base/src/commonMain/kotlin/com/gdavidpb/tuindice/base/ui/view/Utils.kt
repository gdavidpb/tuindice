package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

fun getAnnotatedUrl(
	url: String,
	primary: Color,
	secondary: Color
): AnnotatedString {
	return buildAnnotatedString {
		val hostRange = url.hostRange()

		if (hostRange == null) {
			withStyle(
				style = SpanStyle(
					color = primary,
					textDecoration = TextDecoration.Underline
				)
			) {
				append(url)
			}

			return@buildAnnotatedString
		}

		withStyle(
			style = SpanStyle(
				color = secondary,
				textDecoration = TextDecoration.Underline
			)
		) {
			append(url.substring(0, hostRange.first))

			withStyle(
				style = SpanStyle(color = primary)
			) {
				append(url.substring(hostRange))
			}

			append(url.substring(hostRange.last + 1))
		}
	}
}

private fun String.hostRange(): IntRange? {
	val schemeSeparatorIndex = indexOf("://")
	if (schemeSeparatorIndex < 0) return null

	val hostStart = schemeSeparatorIndex + 3
	if (hostStart >= length) return null

	val hostEnd = indexOfAny(
		chars = charArrayOf('/', '?', '#'),
		startIndex = hostStart
	).takeIf { it >= 0 } ?: length

	if (hostEnd <= hostStart) return null

	return hostStart until hostEnd
}
