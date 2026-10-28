package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.ui.model.WeekStripItem

// The weeks of a sheet of evaluations: the strip of weeks, in the order the Evaluations tab shows them, and the week
// the sheet opens on.
data class EvaluationWeeksItem(
	val strip: List<WeekStripItem<EvaluationWeekKey>>,
	val initialKey: EvaluationWeekKey
)
