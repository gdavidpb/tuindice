package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

// The text with the first occurrence of [emphasis] set in [emphasisStyle] and the rest as it is. A text without the
// emphasis has nothing to set apart, so it stands whole.
internal fun emphasizedText(text: String, emphasis: String, emphasisStyle: SpanStyle): AnnotatedString {
	val start = if (emphasis.isEmpty()) -1 else text.indexOf(emphasis)

	return buildAnnotatedString {
		if (start < 0) {
			append(text)
		} else {
			append(text.substring(0, start))
			withStyle(emphasisStyle) { append(emphasis) }
			append(text.substring(start + emphasis.length))
		}
	}
}
