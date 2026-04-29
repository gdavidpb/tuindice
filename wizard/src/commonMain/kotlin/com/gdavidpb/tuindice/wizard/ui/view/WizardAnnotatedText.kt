package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun String.toWizardAnnotatedText(): AnnotatedString {
	return buildAnnotatedString {
		appendWithBoldMarkers(this@toWizardAnnotatedText)
	}
}

private fun AnnotatedString.Builder.appendWithBoldMarkers(text: String) {
	val marker = "**"
	var currentIndex = 0
	var isBold = false

	while (currentIndex < text.length) {
		val markerIndex = text.indexOf(marker, currentIndex)
		if (markerIndex < 0) {
			appendStyledSegment(
				text = text.substring(currentIndex),
				isBold = isBold
			)
			return
		}

		appendStyledSegment(
			text = text.substring(currentIndex, markerIndex),
			isBold = isBold
		)
		isBold = !isBold
		currentIndex = markerIndex + marker.length
	}
}

private fun AnnotatedString.Builder.appendStyledSegment(
	text: String,
	isBold: Boolean
) {
	if (text.isEmpty()) return

	if (isBold) {
		pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
		append(text)
		pop()
	} else {
		append(text)
	}
}
