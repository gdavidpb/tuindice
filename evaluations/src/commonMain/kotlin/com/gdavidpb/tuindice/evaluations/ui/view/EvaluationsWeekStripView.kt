package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.model.WeekSelectorTags
import com.gdavidpb.tuindice.base.ui.model.WeekStripDayItem
import com.gdavidpb.tuindice.base.ui.model.WeekStripItem
import com.gdavidpb.tuindice.base.ui.model.WeekStripTags
import com.gdavidpb.tuindice.base.ui.view.WeekStripView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

// The week strip of the Evaluations tab: the shared strip of weeks, with the order and the tags of this tab.
@Composable
fun EvaluationsWeekStripView(
	items: List<EvaluationsWeekItem>,
	selectedWeekKey: EvaluationsWeekKey,
	onWeekSelected: (EvaluationsWeekKey) -> Unit,
	modifier: Modifier = Modifier
) {
	val weekItems = remember(items) {
		items
			.sortedBy { item -> item.key.sortOrder }
			.map { item -> item.toWeekStripItem() }
	}

	WeekStripView(
		items = weekItems,
		selectedKey = selectedWeekKey,
		onKeySelected = onWeekSelected,
		tags = EvaluationsWeekStripTags,
		modifier = modifier
	)
}

private fun EvaluationsWeekItem.toWeekStripItem(): WeekStripItem<EvaluationsWeekKey> =
	WeekStripItem(
		key = key,
		tagSuffix = key.tagSuffix,
		// The label arrives as ready text from the mapping, so it is shown as it is.
		labelText = UiText.Raw(labelText),
		days = days.map { day -> day.toWeekStripDayItem() },
		isCurrent = isCurrent
	)

private fun EvaluationWeekDayItem.toWeekStripDayItem(): WeekStripDayItem =
	WeekStripDayItem(
		weekdayText = weekdayText,
		dayText = dayText,
		isSelected = isSelected,
		hasEvaluations = hasEvaluations
	)

// It goes before the strip's tags, which use it: the top-level values of a file are created in order.
private val EvaluationsWeekSelectorTags = WeekSelectorTags(
	label = EvaluationsUiTags.EvaluationsWeekLabel,
	currentIndicator = EvaluationsUiTags.EvaluationsWeekCurrentIndicator
)

private val EvaluationsWeekStripTags = WeekStripTags<EvaluationsWeekKey>(
	strip = EvaluationsUiTags.EvaluationsWeekStrip,
	selector = EvaluationsWeekSelectorTags,
	chip = { key -> EvaluationsUiTags.evaluationsWeekChip(key) },
	page = { key -> EvaluationsUiTags.evaluationsWeekPage(key) }
)
