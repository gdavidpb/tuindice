package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.LocalDate

/**
 * An evaluation of the week. [place] is where its subject has class that day (blocks and room, with
 * no hour), null when it has none; [weekStart] is the Monday of its week, where the Evaluations tab
 * takes the student.
 */
@Immutable
data class SummaryEvaluationRowItem(
	val evaluationId: String,
	val leadText: UiText,
	val titleText: UiText,
	val subjectCode: String,
	val place: ClassPlaceItem?,
	val weekStart: LocalDate
)
