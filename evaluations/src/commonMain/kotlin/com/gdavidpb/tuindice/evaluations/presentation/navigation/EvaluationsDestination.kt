package com.gdavidpb.tuindice.evaluations.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class EvaluationsDestination : Destination() {
	@Serializable
	data object NavGraph : EvaluationsDestination()

	@Serializable
	data object Evaluations : EvaluationsDestination()

	@Serializable
	data class Evaluation(
		val evaluationId: String?
	) : EvaluationsDestination()

	@Serializable
	data class GradePickerDialog(
		val evaluationName: String,
		val subjectCode: String,
		val grade: Double?,
		val maxGrade: Double?
	) : EvaluationsDestination(), DialogDestination

	@Serializable
	data class MaxGradePickerDialog(
		val evaluationName: String,
		val subjectCode: String,
		val grade: Double?
	) : EvaluationsDestination(), DialogDestination

	@Serializable
	data class EvaluationGradePickerDialog(
		val evaluationId: String,
		val evaluationName: String,
		val subjectCode: String,
		val grade: Double,
		val maxGrade: Double
	) : EvaluationsDestination(), DialogDestination

	@Serializable
	data class DeleteEvaluationConfirmationDialog(
		val evaluationId: String
	) : EvaluationsDestination(), DialogDestination
}
