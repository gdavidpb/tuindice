package com.gdavidpb.tuindice.summary.ui.view

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
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaTone

/**
 * A change of the term's index, as a small chip: the tone colours it, the words say its size and direction. The chip
 * reads the same as the chips of Record's term, so a change looks alike in both places.
 */
@Composable
fun TermReportDeltaChip(
	delta: TermReportDeltaItem,
	modifier: Modifier = Modifier
) {
	val colors = deltaColorsOf(tone = delta.tone)

	Box(
		modifier = modifier
			.background(color = colors.container, shape = RoundedCornerShape(TuIndiceRadius.Small))
			.padding(horizontal = TuIndiceSpacing.Medium, vertical = TuIndiceSpacing.XSmall)
	) {
		Text(
			text = delta.text.asString(),
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.SemiBold,
			color = colors.content,
			textAlign = TextAlign.Center
		)
	}
}

private data class DeltaColors(
	val container: Color,
	val content: Color
)

@Composable
private fun deltaColorsOf(tone: TermReportDeltaTone): DeltaColors {
	return when (tone) {
		TermReportDeltaTone.POSITIVE -> DeltaColors(
			container = AcademicStatusColors.approved().copy(alpha = CHIP_ALPHA),
			content = AcademicStatusColors.approved()
		)

		TermReportDeltaTone.NEGATIVE -> DeltaColors(
			container = MaterialTheme.colorScheme.error.copy(alpha = CHIP_ALPHA),
			content = MaterialTheme.colorScheme.error
		)

		TermReportDeltaTone.NEUTRAL -> DeltaColors(
			container = MaterialTheme.colorScheme.surfaceVariant,
			content = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

private const val CHIP_ALPHA = 0.16f
