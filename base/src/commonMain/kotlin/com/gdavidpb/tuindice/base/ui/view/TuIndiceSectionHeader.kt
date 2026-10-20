package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The title of a section of a list: the body text in the medium weight and in sentence case, the same in every screen.
// [modifier] names the node for the tests of the screen that shows it; [topPadding] is the room above it.
@Composable
fun TuIndiceSectionHeader(
	text: String,
	modifier: Modifier = Modifier,
	topPadding: Dp = TuIndiceSpacing.Section
) {
	Text(
		text = text,
		modifier = modifier
			.semantics { heading() }
			.padding(
				start = TuIndiceSpacing.Dialog,
				top = topPadding,
				end = TuIndiceSpacing.Dialog,
				bottom = TuIndiceSpacing.Section
			)
			.fillMaxWidth(),
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
		fontWeight = FontWeight.Medium
	)
}
