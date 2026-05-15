package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun EvaluationMetadataRow(
	modifier: Modifier = Modifier,
	dateIcon: ImageVector,
	dateText: String,
	typeText: String,
	color: Color
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier.size(16.dp),
			imageVector = dateIcon,
			tint = color,
			contentDescription = null
		)

		Text(
			modifier = Modifier.padding(start = 6.dp),
			text = dateText,
			maxLines = 1,
			softWrap = false,
			color = color,
			style = MaterialTheme.typography.bodyMedium
		)

		Text(
			modifier = Modifier.padding(horizontal = 6.dp),
			text = "•",
			color = color,
			style = MaterialTheme.typography.bodyMedium
		)

		Text(
			text = typeText,
			maxLines = 1,
			softWrap = false,
			color = color,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}
