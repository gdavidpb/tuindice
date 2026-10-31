package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun String.toBoldMarkerAnnotatedText(
	boldFontWeight: FontWeight = FontWeight.SemiBold
): AnnotatedString {
	return buildAnnotatedString {
		appendWithBoldMarkers(
			text = this@toBoldMarkerAnnotatedText,
			boldFontWeight = boldFontWeight
		)
	}
}

fun AnnotatedString.Builder.appendWithBoldMarkers(
	text: String,
	boldFontWeight: FontWeight = FontWeight.SemiBold
) {
	val marker = "**"
	var currentIndex = 0

	while (currentIndex < text.length) {
		val opening = text.indexOf(marker, currentIndex)
		val closing = if (opening >= 0) text.indexOf(marker, opening + marker.length) else -1
		if (closing < 0) {
			append(text.substring(currentIndex))
			return
		}

		append(text.substring(currentIndex, opening))
		appendStyledSegment(
			text = text.substring(opening + marker.length, closing),
			boldFontWeight = boldFontWeight
		)
		currentIndex = closing + marker.length
	}
}

private fun AnnotatedString.Builder.appendStyledSegment(
	text: String,
	boldFontWeight: FontWeight
) {
	if (text.isEmpty()) return

	pushStyle(SpanStyle(fontWeight = boldFontWeight))
	append(text)
	pop()
}
