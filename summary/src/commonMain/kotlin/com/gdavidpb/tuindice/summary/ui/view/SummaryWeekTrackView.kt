package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.TuIndiceProgressSegment
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar

/**
 * The weeks of the term as a row of segments on the shared progress bar, apart from each other: the ones gone by
 * (the accent, faded: they read as the progress so far), the one in progress (the full accent, as everything in
 * progress takes it) and the ones to come. The row is one element for a screen reader, which hears [description] and
 * not twelve unnamed shapes.
 */
@Composable
fun SummaryWeekTrackView(
	week: Int,
	totalWeeks: Int,
	description: String,
	modifier: Modifier = Modifier
) {
	val current = MaterialTheme.colorScheme.primary
	val past = current.copy(alpha = PAST_WEEK_ALPHA)
	val future = MaterialTheme.colorScheme.outlineVariant

	TuIndiceProgressBar(
		segments = List(totalWeeks) { index ->
			val number = index + 1

			TuIndiceProgressSegment(
				fraction = 1f / totalWeeks,
				color = when {
					number == week -> current
					number < week -> past
					else -> future
				}
			)
		},
		modifier = modifier.clearAndSetSemantics { contentDescription = description },
		gap = WeekSegmentGap
	)
}

private val WeekSegmentGap = 3.dp
private const val PAST_WEEK_ALPHA = 0.45f
