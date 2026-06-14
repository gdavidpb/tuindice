package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PensumSubjectCodeChip(
	code: String,
	modifier: Modifier = Modifier,
	fallbackContainer: Color = MaterialTheme.colorScheme.surfaceVariant,
	fallbackContent: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
	val chipColors = code.toPensumChipColors(
		fallbackContainer = fallbackContainer,
		fallbackContent = fallbackContent
	)

	Text(
		modifier = modifier
			.background(chipColors.container, PensumElementShape)
			.padding(horizontal = 10.dp, vertical = 6.dp),
		text = code,
		style = MaterialTheme.typography.labelLarge,
		color = chipColors.content,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis
	)
}
