package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * The surface of a block of the hub: `surfaceVariant` with the card radius. The point of attention
 * adds a hairline border ([hasBorder]) to tell itself from the cards that only report.
 */
@Composable
fun SummaryHubCardView(
	modifier: Modifier = Modifier,
	hasBorder: Boolean = false,
	content: @Composable ColumnScope.() -> Unit
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Card)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(shape)
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.then(
				if (hasBorder) {
					Modifier.border(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
				} else {
					Modifier
				}
			),
		content = content
	)
}
