package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone

private val PositiveTermDeltaContainerColor = Color(0xFFC6F0B7)
private val PositiveTermDeltaContentColor = Color(0xFF479A21)
private val NegativeTermDeltaContainerColor = Color(0xFFF2B8BF)
private val NegativeTermDeltaContentColor = Color(0xFF9A212D)
private val TermDeltaChipShape = RoundedCornerShape(TuIndiceRadius.Small)
private val TermDeltaChipHorizontalPadding = 8.dp
private val TermDeltaChipVerticalPadding = 3.dp

@Composable
fun TermDeltaChip(
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
