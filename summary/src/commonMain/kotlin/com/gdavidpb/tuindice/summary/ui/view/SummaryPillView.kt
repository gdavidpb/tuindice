package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * The mark at the start of the first row of the week. "En curso" ([isAccent]) takes the accent, as
 * everything in progress does; the day of the next class ("Hoy", "Lun 19") is a quiet outline.
 *
 * It is [SummaryLeadMinWidth] wide, padding included, and the day that leads an evaluation takes the
 * same width and padding ([summaryLeadPadding]), so at the default letter size the content of the
 * class row starts where the one of the evaluation rows does. With a larger letter it grows to its
 * text instead of cutting it, and the lead column of the rows under it grows the same way.
 */
@Composable
fun SummaryPillView(
	text: String,
	isAccent: Boolean,
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Small)
	val container = if (isAccent) {
		Modifier.background(MaterialTheme.colorScheme.primary, shape)
	} else {
		Modifier.border(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
	}

	Text(
		modifier = modifier
			.widthIn(min = SummaryLeadMinWidth)
			.then(container)
			.summaryLeadPadding(vertical = TuIndiceSpacing.XSmall),
		text = text,
		style = MaterialTheme.typography.labelMedium,
		fontWeight = if (isAccent) FontWeight.Bold else FontWeight.Medium,
		color = if (isAccent) {
			MaterialTheme.colorScheme.onPrimary
		} else {
			MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
		},
		textAlign = TextAlign.Center
	)
}

/** The width, padding included, the pill and the day that leads an evaluation share, so the rows line up. */
internal val SummaryLeadMinWidth = 64.dp

/** The horizontal padding of the pill and of the day that leads an evaluation: the same on both. */
internal fun Modifier.summaryLeadPadding(vertical: Dp = TuIndiceSpacing.None): Modifier =
	padding(horizontal = TuIndiceSpacing.XSmall, vertical = vertical)
