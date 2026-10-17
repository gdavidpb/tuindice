package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Trimestre": [linkText] is the link of its header, the name of the term in progress ("Sep - Dic 2026"), "Registro"
 * when there is none, and absent for a new student. [week] is absent outside weeks 1 to 12, [summaryText] says how many
 * subjects there are and their credits, and [subjects] are the points of each subject, absent while not known.
 * [finished] is the last term that is over, when no term is in progress and there is one. [card] is there when the term
 * has nothing else to show: the invitation to plan a term, the first one to come, or what is still to download.
 * [termId] is the record's id of the term in progress, the one the assistant names; absent when there is none.
 */
@Immutable
data class SummaryTermItem(
	val linkText: UiText?,
	val week: SummaryTermWeekItem?,
	val summaryText: UiText?,
	val subjects: List<SummarySubjectPointsItem>,
	val finished: SummaryFinishedTermItem?,
	val card: SummaryCardItem?,
	val termId: String? = null
)
