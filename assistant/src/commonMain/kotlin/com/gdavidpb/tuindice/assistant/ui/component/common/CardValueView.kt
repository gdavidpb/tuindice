package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

// The figure at the end of a row, bold and with tabular numbers so columns of figures line up. It ends the row, and a
// long one wraps onto a second line instead of pushing the name out of its place.
@Composable
fun CardValueView(
	text: String,
	modifier: Modifier = Modifier
) {
	Text(
		text = text,
		modifier = modifier,
		style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = TABULAR_NUMBERS),
		fontWeight = FontWeight.SemiBold,
		color = MaterialTheme.colorScheme.onSurface,
		textAlign = TextAlign.End,
		maxLines = 2
	)
}

// The OpenType feature that gives every digit the same width, so the figures of a card line up. It is the only place
// the feature is written: the figures of the big values use it too.
internal const val TABULAR_NUMBERS = "tnum"
