package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Trimestre": [nameText] is the link to the Record ("Sep - Dic 2026"); [week] is absent outside
 * weeks 1 to 12, and [subjects] are the points of each subject, absent while not known.
 */
@Immutable
data class SummaryTermItem(
	val nameText: UiText,
	val week: SummaryTermWeekItem?,
	val subjects: List<SummarySubjectPointsItem>
)
