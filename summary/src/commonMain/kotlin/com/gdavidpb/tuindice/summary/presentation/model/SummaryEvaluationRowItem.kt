package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.LocalDate

/**
 * An evaluation of the week, shown the way its row reads in the Evaluations tab: [typeIcon] is the icon
 * of its type, [titleText] its name in capitals and [statusText] its state, with [isOverdue] for the
 * error tone. [statusText] is null when the evaluation is scheduled: its row then has no state chip. [place]
 * is where its subject has class that day (room and blocks, with no hour), null when it has none; [weekStart]
 * is the Monday of its week, where the Evaluations tab takes the student.
 */
@Immutable
data class SummaryEvaluationRowItem(
	val evaluationId: String,
	val leadText: UiText,
	val typeIcon: ImageVector,
	val titleText: UiText,
	val statusText: UiText?,
	val isOverdue: Boolean,
	val subjectCode: String,
	val place: ClassPlaceItem?,
	val weekStart: LocalDate
)
