package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.utils.extension.forEachBoldMarkerSegment

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
	text.forEachBoldMarkerSegment { segment, isBold ->
		if (isBold) appendStyledSegment(text = segment, boldFontWeight = boldFontWeight) else append(segment)
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
