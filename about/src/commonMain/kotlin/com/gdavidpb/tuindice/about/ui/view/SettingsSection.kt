package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * A group of Settings: its small title in the accent of the theme, and its rows under it. The accent is `secondary`,
 * the tone of the brand that stays legible as text on both themes (the yellow primary is for fills, not for words).
 */
@Composable
fun SettingsSection(
	title: String,
	content: @Composable () -> Unit
) {
	Text(
		text = title,
		modifier = Modifier
			.testTag(SettingsUiTags.SectionTitle)
			.semantics { heading() }
			.padding(
				start = TuIndiceSpacing.Screen,
				top = TuIndiceSpacing.Dialog,
				end = TuIndiceSpacing.Screen,
				bottom = TuIndiceSpacing.Medium
			)
			.fillMaxWidth(),
		style = MaterialTheme.typography.labelLarge,
		color = MaterialTheme.colorScheme.secondary,
		fontWeight = FontWeight.Medium
	)

	content()
}
