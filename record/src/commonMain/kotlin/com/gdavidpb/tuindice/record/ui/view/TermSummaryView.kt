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
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.term_credits_sum_label
import tuindice.record.generated.resources.term_grade_label
import tuindice.record.generated.resources.term_grade_sum_label

private val PositiveTermDeltaContainerColor = Color(0xFFC6F0B7)
private val PositiveTermDeltaContentColor = Color(0xFF479A21)
private val NegativeTermDeltaContainerColor = Color(0xFFF2B8BF)
private val NegativeTermDeltaContentColor = Color(0xFF9A212D)
private val TermDeltaChipShape = RoundedCornerShape(8.dp)
private val TermDeltaChipHorizontalPadding = 8.dp
private val TermDeltaChipVerticalPadding = 3.dp

@Composable
fun TermSummaryView(
	modifier: Modifier = Modifier,
	item: TermItem
) {
	TermSummaryContent(
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
internal fun TermSummaryContent(
	modifier: Modifier = Modifier,
	item: TermItem
) {
	val termGradeLabel = stringResource(Res.string.term_grade_label)
	val termGradeSumLabel = stringResource(Res.string.term_grade_sum_label)
	val termCreditsSumLabel = stringResource(Res.string.term_credits_sum_label)

	Column(modifier = modifier) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(IntrinsicSize.Min)
				.padding(vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			TermMetricItem(
				modifier = Modifier.weight(1f),
				value = item.gradeText,
				delta = item.gradeDelta,
				subtitle = termGradeLabel
			)

			TermMetricDivider()

			TermMetricItem(
				modifier = Modifier.weight(1f),
				value = item.gradeSumText,
				delta = item.gradeSumDelta,
				subtitle = termGradeSumLabel
			)

			TermMetricDivider()

			TermMetricItem(
				modifier = Modifier.weight(1f),
				value = item.creditsText,
				delta = item.creditsDelta,
				subtitle = termCreditsSumLabel
			)
		}
	}
}

@Composable
private fun TermMetricItem(
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
			fontWeight = FontWeight.SemiBold,
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

@Composable
private fun TermDeltaChip(
	modifier: Modifier = Modifier,
	delta: TermMetricDelta
) {
	val chipColors = termDeltaChipColors(
		tone = delta.tone,
		primaryContainer = MaterialTheme.colorScheme.primaryContainer,
		onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
	)

	Box(
		modifier = modifier
			.background(
				color = chipColors.containerColor,
				shape = TermDeltaChipShape
			)
			.padding(
				horizontal = TermDeltaChipHorizontalPadding,
				vertical = TermDeltaChipVerticalPadding
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

internal fun termDeltaChipColors(
	tone: TermMetricDeltaTone,
	primaryContainer: Color,
	onPrimaryContainer: Color
): TermDeltaChipColors = when (tone) {
	TermMetricDeltaTone.Positive -> TermDeltaChipColors(
		containerColor = PositiveTermDeltaContainerColor,
		contentColor = PositiveTermDeltaContentColor
	)

	TermMetricDeltaTone.Negative -> TermDeltaChipColors(
		containerColor = NegativeTermDeltaContainerColor,
		contentColor = NegativeTermDeltaContentColor
	)

	TermMetricDeltaTone.Neutral -> TermDeltaChipColors(
		containerColor = primaryContainer,
		contentColor = onPrimaryContainer
	)

	TermMetricDeltaTone.Informational -> TermDeltaChipColors(
		containerColor = primaryContainer,
		contentColor = onPrimaryContainer
	)
}

internal data class TermDeltaChipColors(
	val containerColor: Color,
	val contentColor: Color
)

@Composable
private fun TermMetricDivider() {
	Box(
		modifier = Modifier
			.padding(vertical = 8.dp)
			.fillMaxHeight()
			.width(1.dp)
			.background(MaterialTheme.colorScheme.outlineVariant)
	)
}
