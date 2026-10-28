package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.model.WeekStripDayItem
import com.gdavidpb.tuindice.base.ui.view.WeekDayView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem

// A day of the Evaluations tab's week strip, drawn by the shared day view that the strip uses as well. This binding is
// what the tab's own tests of a day use.
@Composable
fun EvaluationWeekDayView(
	item: EvaluationWeekDayItem,
	modifier: Modifier = Modifier
) {
	WeekDayView(
		item = item.toWeekStripDayItem(),
		modifier = modifier
	)
}

internal fun EvaluationWeekDayItem.toWeekStripDayItem(): WeekStripDayItem =
	WeekStripDayItem(
		weekdayText = weekdayText,
		dayText = dayText,
		isSelected = isSelected,
		hasEvaluations = hasEvaluations
	)
