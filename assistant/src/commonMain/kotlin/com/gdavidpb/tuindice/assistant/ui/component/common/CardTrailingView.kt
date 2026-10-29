package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The figure and the chips at the end of a row: the figure first, then the chips, in that order on every card. They are
// centred on each other, and when they do not fit beside the name they wrap onto a second line, from the end. The
// [mutedSuffix] of the figure is drawn in the muted tone, as CardValueView draws it.
@Composable
fun CardTrailingView(
	value: String?,
	chips: List<CardChipItem>,
	modifier: Modifier = Modifier,
	mutedSuffix: String? = null
) {
	FlowRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small, Alignment.End),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		value?.let { text ->
			CardValueView(
				text = text,
				modifier = Modifier.align(Alignment.CenterVertically),
				mutedSuffix = mutedSuffix
			)
		}
		chips.forEach { chip ->
			CardChipView(
				text = chip.text.asString(),
				tone = chip.tone,
				modifier = Modifier.align(Alignment.CenterVertically)
			)
		}
	}
}
