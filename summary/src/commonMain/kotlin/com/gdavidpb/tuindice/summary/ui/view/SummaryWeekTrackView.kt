package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp

/**
 * The weeks of the term as a row of segments: the ones gone by, the one in progress (the accent, as
 * everything in progress takes it) and the ones to come. The row is one element for a screen reader,
 * which hears [description] and not twelve unnamed shapes.
 */
@Composable
fun SummaryWeekTrackView(
	week: Int,
	totalWeeks: Int,
	description: String,
	modifier: Modifier = Modifier
) {
	val past = MaterialTheme.colorScheme.onSurfaceVariant
	val current = MaterialTheme.colorScheme.primary
	val future = MaterialTheme.colorScheme.outlineVariant

	Row(
		modifier = modifier
			.fillMaxWidth()
			.clearAndSetSemantics { contentDescription = description },
		horizontalArrangement = Arrangement.spacedBy(WeekSegmentGap)
	) {
		repeat(totalWeeks) { index ->
			val number = index + 1

			WeekSegment(
				color = when {
					number == week -> current
					number < week -> past
					else -> future
				}
			)
		}
	}
}

@Composable
private fun RowScope.WeekSegment(color: Color) {
	Box(
		modifier = Modifier
			.weight(1f)
			.height(WeekSegmentHeight)
			.background(color, RoundedCornerShape(WeekSegmentHeight / 2))
	)
}

private val WeekSegmentGap = 3.dp
private val WeekSegmentHeight = 6.dp
