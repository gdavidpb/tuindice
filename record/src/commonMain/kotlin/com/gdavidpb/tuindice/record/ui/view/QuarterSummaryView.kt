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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDeltaTone
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.quarter_credits_sum_label
import tuindice.record.generated.resources.quarter_grade_label
import tuindice.record.generated.resources.quarter_grade_sum_label

private val PositiveQuarterDeltaContainerColor = Color(0xFFC6F0B7)
private val PositiveQuarterDeltaContentColor = Color(0xFF479A21)
private val NegativeQuarterDeltaContainerColor = Color(0xFFF2B8BF)
private val NegativeQuarterDeltaContentColor = Color(0xFF9A212D)
private val QuarterDeltaChipShape = RoundedCornerShape(8.dp)
private val QuarterDeltaChipHorizontalPadding = 8.dp
private val QuarterDeltaChipVerticalPadding = 3.dp

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
				delta = item.creditsDelta,
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
			fontWeight = FontWeight.SemiBold,
			textAlign = TextAlign.Center
		)

		if (delta != null) {
			QuarterDeltaChip(
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

@Composable
private fun QuarterDeltaChip(
	modifier: Modifier = Modifier,
	delta: QuarterMetricDelta
) {
	val chipColors = when (delta.tone) {
		QuarterMetricDeltaTone.Positive -> QuarterDeltaChipColors(
			containerColor = PositiveQuarterDeltaContainerColor,
			contentColor = PositiveQuarterDeltaContentColor
		)

		QuarterMetricDeltaTone.Negative -> QuarterDeltaChipColors(
			containerColor = NegativeQuarterDeltaContainerColor,
			contentColor = NegativeQuarterDeltaContentColor
		)

		QuarterMetricDeltaTone.Neutral -> QuarterDeltaChipColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant,
			contentColor = MaterialTheme.colorScheme.onSurfaceVariant
		)

		QuarterMetricDeltaTone.Informational -> QuarterDeltaChipColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer,
			contentColor = MaterialTheme.colorScheme.onPrimaryContainer
		)
	}

	Box(
		modifier = modifier
			.background(
				color = chipColors.containerColor,
				shape = QuarterDeltaChipShape
			)
			.padding(
				horizontal = QuarterDeltaChipHorizontalPadding,
				vertical = QuarterDeltaChipVerticalPadding
			)
	) {
		Text(
			text = delta.text,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.SemiBold,
			color = chipColors.contentColor,
			textAlign = TextAlign.Center
		)
	}
}

private data class QuarterDeltaChipColors(
	val containerColor: Color,
	val contentColor: Color
)

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
