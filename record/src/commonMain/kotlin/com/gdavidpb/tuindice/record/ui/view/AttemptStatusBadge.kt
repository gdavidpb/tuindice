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
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius

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
				shape = RoundedCornerShape(TuIndiceRadius.Small)
			)
			.padding(vertical = 4.dp, horizontal = 10.dp),
		text = text,
		color = contentColor,
		style = MaterialTheme.typography.labelLarge
	)
}
