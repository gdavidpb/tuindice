package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDeltaTone
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.quarter_credits_sum_label
import tuindice.record.generated.resources.quarter_grade_label
import tuindice.record.generated.resources.quarter_grade_sum_label

private val PositiveQuarterDeltaColor = Color(0xFF2E7D32)

@Composable
fun QuarterSummaryView(
	modifier: Modifier = Modifier,
	item: QuarterItem
) {
	QuarterSummaryContent(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 8.dp
			),
		item = item
	)
}

@Composable
internal fun QuarterSummaryContent(
	modifier: Modifier = Modifier,
	item: QuarterItem
) {
	val quarterGradeLabel = stringResource(Res.string.quarter_grade_label)
	val quarterGradeSumLabel = stringResource(Res.string.quarter_grade_sum_label)
	val quarterCreditsSumLabel = stringResource(Res.string.quarter_credits_sum_label)

	Column(modifier = modifier) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(IntrinsicSize.Min)
				.padding(vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			QuarterMetricItem(
				modifier = Modifier.weight(1f),
				value = item.gradeText,
				delta = item.gradeDelta,
				subtitle = quarterGradeLabel
			)

			QuarterMetricDivider()

			QuarterMetricItem(
				modifier = Modifier.weight(1f),
				value = item.gradeSumText,
				delta = item.gradeSumDelta,
				subtitle = quarterGradeSumLabel
			)

			QuarterMetricDivider()

			QuarterMetricItem(
				modifier = Modifier.weight(1f),
				value = item.creditsText,
				subtitle = quarterCreditsSumLabel
			)
		}
	}
}

@Composable
private fun QuarterMetricItem(
	modifier: Modifier = Modifier,
	value: AnnotatedString,
	delta: QuarterMetricDelta? = null,
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
			fontWeight = FontWeight.Medium,
			textAlign = TextAlign.Center
		)
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 4.dp),
			text = subtitle,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center
		)

		if (delta != null) {
			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 2.dp),
				text = delta.text,
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				color = when (delta.tone) {
					QuarterMetricDeltaTone.Positive -> PositiveQuarterDeltaColor
					QuarterMetricDeltaTone.Negative -> MaterialTheme.colorScheme.error
					QuarterMetricDeltaTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
				},
				textAlign = TextAlign.Center
			)
		}
	}
}

@Composable
private fun QuarterMetricDivider() {
	Box(
		modifier = Modifier
			.padding(vertical = 8.dp)
			.fillMaxHeight()
			.width(1.dp)
			.background(MaterialTheme.colorScheme.outlineVariant)
	)
}
