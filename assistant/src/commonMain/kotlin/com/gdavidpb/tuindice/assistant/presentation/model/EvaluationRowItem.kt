package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import kotlinx.datetime.LocalDate

// One evaluation: its subject, its title, its date when it has one, its grade over the maximum, its state, and the
// week it falls in. The sheet draws the title in capitals with the date on a line of its own, as the Evaluations tab
// does; the chat card joins the title and the short date in [nameText]. [deviceCard] is the card the Evaluations tab
// shows for the same evaluation, when the device has it: it is drawn instead of the values above, which stay for the
// week the row is filed under and for an evaluation the device does not have.
data class EvaluationRowItem(
	val code: String,
	val title: String,
	val date: LocalDate?,
	val dateText: UiText,
	val nameText: UiText,
	val gradeText: UiText,
	val status: EvaluationStatusItem?,
	val weekKey: EvaluationWeekKey?,
	val deviceCard: EvaluationCardItem? = null
)
