package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration

// A link of a chat card: underlined, in the text tone, as the approved chat style asks.
@Composable
fun CardLinkView(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Text(
		text = text,
		modifier = modifier.clickable(role = Role.Button, onClick = onClick),
		style = MaterialTheme.typography.bodyMedium,
		textDecoration = TextDecoration.Underline,
		color = MaterialTheme.colorScheme.onSurface
	)
}
