package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

// The figure at the right of a card row, bold and with tabular numbers so columns of figures line up.
@Composable
fun CardValueView(
	text: String,
	modifier: Modifier = Modifier
) {
	Text(
		text = text,
		modifier = modifier,
		style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
		fontWeight = FontWeight.SemiBold,
		color = MaterialTheme.colorScheme.onSurface
	)
}
