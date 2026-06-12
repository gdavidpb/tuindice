package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta

@Composable
fun TermMetricItem(
	modifier: Modifier = Modifier,
	value: AnnotatedString,
	delta: TermMetricDelta? = null,
	subtitle: String
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text(
			modifier = Modifier.fillMaxWidth(),
			text = value,
			style = MaterialTheme.typography.titleMedium,
			textAlign = TextAlign.Center
		)

		if (delta != null) {
			TermDeltaChip(
				modifier = Modifier
					.padding(top = 6.dp),
				delta = delta
			)
		}

		Text(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = if (delta != null) 6.dp else 4.dp),
			text = subtitle,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			fontWeight = FontWeight.Medium,
			textAlign = TextAlign.Center
		)
	}
}
