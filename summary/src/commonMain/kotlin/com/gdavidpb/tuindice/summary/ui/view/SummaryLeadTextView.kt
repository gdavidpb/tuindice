package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha

/**
 * The day that leads an evaluation of the week ("Hoy", "Vie 16"), lighter than the row it opens. It
 * has the width and the padding of the pill of the class row, so both kinds of row line up.
 */
@Composable
fun SummaryLeadTextView(
	text: String,
	modifier: Modifier = Modifier
) {
	Text(
		modifier = modifier
			.widthIn(min = SummaryLeadMinWidth)
			.summaryLeadPadding(),
		text = text,
		style = MaterialTheme.typography.labelLarge,
		fontWeight = FontWeight.Medium,
		color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
	)
}
