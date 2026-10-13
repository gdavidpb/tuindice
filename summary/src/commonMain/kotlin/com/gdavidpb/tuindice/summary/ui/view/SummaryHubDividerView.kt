package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/** The hairline between two rows of a card, inset by the screen margin on both sides. */
@Composable
fun SummaryHubDividerView(modifier: Modifier = Modifier) {
	HorizontalDivider(
		modifier = modifier.padding(horizontal = TuIndiceSpacing.Screen),
		thickness = TuIndiceSpacing.Hairline,
		color = MaterialTheme.colorScheme.outlineVariant
	)
}
