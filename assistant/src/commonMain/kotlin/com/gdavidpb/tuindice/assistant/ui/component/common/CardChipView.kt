package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// A state shown as a chip («Aprobada», «En curso», «Pesada»). A neutral chip is outlined in the muted tone, as every
// chip was. A toned chip takes its tone in the text and the outline, over a soft fill of the same tone.
@Composable
fun CardChipView(
	text: String,
	tone: CardChipTone = CardChipTone.Neutral,
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Large)
	val toneColor = tone.color()
	val contentColor = toneColor ?: MaterialTheme.colorScheme.onSurfaceVariant
	val outlineColor = toneColor?.copy(alpha = OUTLINE_ALPHA) ?: MaterialTheme.colorScheme.outlineVariant

	Text(
		text = text,
		modifier = modifier
			.then(toneColor?.let { color -> Modifier.background(color.copy(alpha = FILL_ALPHA), shape) } ?: Modifier)
			.border(TuIndiceSpacing.Hairline, outlineColor, shape)
			.padding(horizontal = TuIndiceSpacing.Medium, vertical = TuIndiceSpacing.XSmall),
		style = MaterialTheme.typography.labelSmall,
		color = contentColor
	)
}

// The colour of a tone, or null for the neutral one, which keeps the muted look of a plain chip.
@Composable
private fun CardChipTone.color(): Color? =
	when (this) {
		CardChipTone.Neutral -> null
		CardChipTone.Approved -> AcademicStatusColors.approved()
		CardChipTone.Current -> MaterialTheme.colorScheme.primary
		CardChipTone.Failed -> MaterialTheme.colorScheme.error
		CardChipTone.Success -> AcademicStatusColors.success()
		CardChipTone.Warning -> AcademicStatusColors.warning()
		CardChipTone.Muted -> AcademicStatusColors.available()
	}

// The outline of a toned chip, and the fill under it, as a share of the tone.
private const val OUTLINE_ALPHA = 0.35f
private const val FILL_ALPHA = 0.14f
