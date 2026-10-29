package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

// The short label on top of a chat card («SEP–DIC 2026», «AVANCE DEL PENSUM»): small capitals of the muted tone. It is
// a heading for a screen reader, which reads the words as they are written and not in capitals.
@Composable
fun CardLabelView(
	text: String,
	modifier: Modifier = Modifier
) {
	Text(
		text = text.uppercase(),
		modifier = modifier.semantics {
			heading()
			contentDescription = text
		},
		style = MaterialTheme.typography.labelMedium,
		fontWeight = FontWeight.SemiBold,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}
