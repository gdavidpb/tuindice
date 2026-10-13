package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsExplanation
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.LocalDate

/**
 * Internal machine inputs for the evaluations list: observation outcomes split per
 * resolution (so every row keeps a fixed declared target), the refresh lifecycle, the
 * request to show a week and the effect cycles of the grade dialog, grade save and removal.
 */
sealed interface EvaluationsInternalEvent {
	data object EvaluationsWaitingObserved : EvaluationsInternalEvent

	data class EvaluationsRecordDataUnavailableObserved(
		val explanation: EvaluationsExplanation
	) : EvaluationsInternalEvent

	data class EvaluationsNoAttemptsObserved(
		val explanation: EvaluationsExplanation
	) : EvaluationsInternalEvent

	data class EvaluationsContentObserved(
		val weekItems: List<EvaluationsWeekItem>,
		val defaultWeekKey: EvaluationsWeekKey,
		val evaluationWeekGroups: List<EvaluationsWeekGroupItem>,
		val weekOneStart: LocalDate? = null,
		// Whether some subject of the current term has a class schedule to open.
		val hasSchedule: Boolean = false
	) : EvaluationsInternalEvent

	// A request to show the week that this Monday opens is pending: whoever sees it (the observation,
	// the content having just loaded, or the request itself) sends it, and only the first to take it
	// from the request applies it.
	data class WeekFocusRequested(
		val weekStart: LocalDate
	) : EvaluationsInternalEvent

	data class EvaluationsEmptyObserved(
		// Whether some subject of the current term has a class schedule to open.
		val hasSchedule: Boolean = false
	) : EvaluationsInternalEvent

	data class EvaluationsEmptyConfirmed(
		val hasSchedule: Boolean = false
	) : EvaluationsInternalEvent

	data class EvaluationsObservationFailed(
		val message: String
	) : EvaluationsInternalEvent

	data object EvaluationsRefreshStarted : EvaluationsInternalEvent

	data class EvaluationsRefreshFailed(
		val message: String
	) : EvaluationsInternalEvent

	data class GradePickerLoaded(
		val evaluationId: String,
		val evaluationName: String,
		val subjectCode: String,
		val grade: Double,
		val maxGrade: Double
	) : EvaluationsInternalEvent

	data class GradePickerLoadFailed(
		val message: String
	) : EvaluationsInternalEvent

	data class EvaluationGradeSaved(
		val message: String
	) : EvaluationsInternalEvent

	data class EvaluationGradeSaveFailed(
		val message: String
	) : EvaluationsInternalEvent

	data class EvaluationRemoved(
		val message: String
	) : EvaluationsInternalEvent

	data class EvaluationRemoveFailed(
		val message: String
	) : EvaluationsInternalEvent
}
