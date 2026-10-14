package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Trimestre": [nameText] is the link to the Record ("Sep - Dic 2026"); [week] is absent outside weeks 1 to 12,
 * [summaryText] says how many subjects there are and their credits ("4 materias · 16 UC") and is absent with no
 * subject, and [subjects] are the points of each subject, absent while not known.
 */
@Immutable
data class SummaryTermItem(
	val nameText: UiText,
	val week: SummaryTermWeekItem?,
	val summaryText: UiText?,
	val subjects: List<SummarySubjectPointsItem>
)
