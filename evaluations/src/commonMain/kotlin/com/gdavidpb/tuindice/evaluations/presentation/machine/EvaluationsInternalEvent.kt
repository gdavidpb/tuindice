package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey

/**
 * Internal machine inputs for the evaluations list: observation outcomes split per
 * resolution (so every row keeps a fixed declared target), the refresh lifecycle, and
 * the effect cycles of the grade dialog, grade save and removal.
 */
sealed interface EvaluationsInternalEvent {
	data object EvaluationsWaitingObserved : EvaluationsInternalEvent

	data object EvaluationsRecordDataUnavailableObserved : EvaluationsInternalEvent

	data class EvaluationsNoAttemptsObserved(
		val reason: EvaluationsNoAttemptsReason
	) : EvaluationsInternalEvent

	data class EvaluationsContentObserved(
		val weekItems: List<EvaluationsWeekItem>,
		val defaultWeekKey: EvaluationsWeekKey,
		val evaluationWeekGroups: List<EvaluationsWeekGroupItem>
	) : EvaluationsInternalEvent

	data object EvaluationsEmptyObserved : EvaluationsInternalEvent

	data object EvaluationsEmptyConfirmed : EvaluationsInternalEvent

	data object EvaluationsObservationFailed : EvaluationsInternalEvent

	data object EvaluationsRefreshStarted : EvaluationsInternalEvent

	data object EvaluationsRefreshFailed : EvaluationsInternalEvent

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
