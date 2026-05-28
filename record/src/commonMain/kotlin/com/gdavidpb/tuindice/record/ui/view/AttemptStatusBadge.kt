package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AttemptStatusBadge(
	modifier: Modifier = Modifier,
	text: String,
	containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
	contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
	Text(
		modifier = modifier
			.background(
				color = containerColor,
				shape = RoundedCornerShape(8.dp)
			)
			.padding(vertical = 4.dp, horizontal = 10.dp),
		text = text,
		color = contentColor,
		style = MaterialTheme.typography.labelLarge
	)
}
