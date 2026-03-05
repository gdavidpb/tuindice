package com.gdavidpb.tuindice.about.ui.custom

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.about.ui.AboutUiTags

@Composable
fun AboutHeader(
	text: String,
	content: @Composable () -> Unit
) {
	Text(
		text = text,
		modifier = Modifier
			.testTag(AboutUiTags.HeaderTitle)
			.padding(
				horizontal = 24.dp,
				vertical = 16.dp
			)
			.fillMaxWidth(),
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
		fontWeight = FontWeight.Medium
	)

	content()
}
