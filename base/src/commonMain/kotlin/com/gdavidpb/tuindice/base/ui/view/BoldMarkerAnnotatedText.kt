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
	var isBold = false

	while (currentIndex < text.length) {
		val markerIndex = text.indexOf(marker, currentIndex)
		if (markerIndex < 0) {
			appendStyledSegment(
				text = text.substring(currentIndex),
				isBold = isBold,
				boldFontWeight = boldFontWeight
			)
			return
		}

		appendStyledSegment(
			text = text.substring(currentIndex, markerIndex),
			isBold = isBold,
			boldFontWeight = boldFontWeight
		)
		isBold = !isBold
		currentIndex = markerIndex + marker.length
	}
}

private fun AnnotatedString.Builder.appendStyledSegment(
	text: String,
	isBold: Boolean,
	boldFontWeight: FontWeight
) {
	if (text.isEmpty()) return

	if (isBold) {
		pushStyle(SpanStyle(fontWeight = boldFontWeight))
		append(text)
		pop()
	} else {
		append(text)
	}
}
