package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView

// A component this build cannot render shows its fallback text, in a card.
@Composable
fun ChatFallbackComponentView(
	fallbackText: String,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		CardBodyTextView(text = fallbackText)
	}
}
