package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.model.WeekSelectorTags
import com.gdavidpb.tuindice.base.ui.view.WeekSelectorItemView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

// The label of a week of the Evaluations tab's week strip, drawn by the shared week view with the tags of this tab.
// [showCurrentIndicator] leaves out the dot of the week that holds today.
@Composable
fun EvaluationWeekSelectorItemView(
	item: EvaluationsWeekItem,
	isSelected: Boolean,
	showCurrentIndicator: Boolean = true,
	modifier: Modifier = Modifier
) {
	WeekSelectorItemView(
		labelText = item.labelText,
		isCurrent = showCurrentIndicator && item.isCurrent,
		isSelected = isSelected,
		tags = EvaluationsWeekSelectorTags,
		modifier = modifier
	)
}

// The tags of the weeks of the Evaluations tab's week strip.
internal val EvaluationsWeekSelectorTags = WeekSelectorTags(
	label = EvaluationsUiTags.EvaluationsWeekLabel,
	currentIndicator = EvaluationsUiTags.EvaluationsWeekCurrentIndicator
)
