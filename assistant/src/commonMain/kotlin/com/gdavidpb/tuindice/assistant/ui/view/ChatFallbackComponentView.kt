package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// A component this build cannot render shows its fallback text, in a card.
@Composable
fun ChatFallbackComponentView(
	fallbackText: String,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = fallbackText,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}
