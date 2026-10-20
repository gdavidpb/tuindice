package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// The text of a chat card that is neither a label, a title nor a figure. A muted text is the detail that reads after
// the main line of the card.
@Composable
fun CardBodyTextView(
	text: String,
	modifier: Modifier = Modifier,
	muted: Boolean = false
) {
	Text(
		text = text,
		modifier = modifier,
		style = MaterialTheme.typography.bodyMedium,
		color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
	)
}
