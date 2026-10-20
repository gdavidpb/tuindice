package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha

// The figure at the end of a row, bold and with tabular numbers so columns of figures line up. It ends the row, and a
// long one wraps onto as many lines as it needs, without pushing the name out of its place. A [mutedSuffix] the text
// ends with is drawn in the muted tone and at the regular weight: the hours that follow the blocks of a class.
@Composable
fun CardValueView(
	text: String,
	modifier: Modifier = Modifier,
	mutedSuffix: String? = null
) {
	Text(
		text = text.withMutedSuffix(
			suffix = mutedSuffix,
			mutedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
		),
		modifier = modifier,
		style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = TABULAR_NUMBERS),
		fontWeight = FontWeight.SemiBold,
		color = MaterialTheme.colorScheme.onSurface,
		textAlign = TextAlign.End
	)
}

// The text with its muted suffix in the muted color and the regular weight. A suffix the text does not end with leaves
// the text as it is.
private fun String.withMutedSuffix(suffix: String?, mutedColor: Color): AnnotatedString {
	if (suffix.isNullOrEmpty() || !endsWith(suffix)) return AnnotatedString(this)

	// Cut before the builder opens: inside it, length is the length of the text built so far, not of this text.
	val lead = substring(0, length - suffix.length)

	return buildAnnotatedString {
		append(lead)
		withStyle(SpanStyle(color = mutedColor, fontWeight = FontWeight.Normal)) { append(suffix) }
	}
}

// The OpenType feature that gives every digit the same width, so the figures of a card line up. It is the only place
// the feature is written: the figures of the big values use it too.
internal const val TABULAR_NUMBERS = "tnum"
