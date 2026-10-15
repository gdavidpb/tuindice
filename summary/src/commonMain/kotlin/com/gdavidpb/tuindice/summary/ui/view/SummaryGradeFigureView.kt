package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * A grade as a figure, with its [label] beside it on the same baseline: the grade over its scale, written as the header
 * writes it, at the [style] that the surface keeps for its figure, and the label after it in the body style. [tag]
 * names the grade's text, so each surface names its own.
 */
@Composable
fun SummaryGradeFigureView(
	grade: Float,
	label: String,
	style: TextStyle,
	tag: String,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.Top
	) {
		GradeTextView(
			modifier = Modifier.alignByBaseline(),
			grade = grade,
			style = style,
			tag = tag
		)

		Text(
			modifier = Modifier
				.weight(1f)
				.alignByBaseline(),
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
