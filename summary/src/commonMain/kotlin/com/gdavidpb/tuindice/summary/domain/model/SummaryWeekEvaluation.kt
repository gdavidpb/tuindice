package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import kotlinx.datetime.LocalDate

/**
 * An evaluation of this week that has not gone by. [ordinal] is the position it has among the
 * evaluations of the same type of its subject, as the Evaluations tab numbers them ("Parcial 1"),
 * [meeting] the class of its subject that day, where it takes place, if there is one, and [state]
 * the state the Evaluations tab gives it on that day, told by the same rule ("Programada").
 */
data class SummaryWeekEvaluation(
	val evaluationId: String,
	val subjectCode: String,
	val type: EvaluationType,
	val ordinal: Int,
	val date: LocalDate,
	val isToday: Boolean,
	val meeting: ScheduleMeeting?,
	val state: EvaluationState
)
