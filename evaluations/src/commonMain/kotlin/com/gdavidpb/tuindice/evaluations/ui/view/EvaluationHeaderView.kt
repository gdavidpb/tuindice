package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationHeaderView(
	label: String,
	countText: String,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.background(MaterialTheme.colorScheme.background)
			.fillMaxWidth()
			.padding(
				top = 14.dp,
				bottom = 6.dp,
				start = 20.dp,
				end = 20.dp
			),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			modifier = Modifier
				.testTag(EvaluationsUiTags.evaluationHeader(label))
				.weight(1f),
			text = label,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)

		Spacer(modifier = Modifier.width(12.dp))

		Text(
			text = countText,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Medium,
			maxLines = 1
		)
	}
}
