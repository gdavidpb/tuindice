package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// A progress bar of a chat card: the one place inside a card where the accent tone appears.
@Composable
fun CardProgressView(
	fraction: Float,
	modifier: Modifier = Modifier
) {
	LinearProgressIndicator(
		progress = { fraction.coerceIn(0f, 1f) },
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.primary,
		trackColor = MaterialTheme.colorScheme.outlineVariant,
		strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
	)
}
