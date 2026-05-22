package com.gdavidpb.tuindice.evaluations.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
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
	) : EvaluationsDestination()

	@Serializable
	data class MaxGradePickerDialog(
		val evaluationName: String,
		val subjectCode: String,
		val grade: Double?
	) : EvaluationsDestination()

	@Serializable
	data class EvaluationGradePickerDialog(
		val evaluationId: String,
		val evaluationName: String,
		val subjectCode: String,
		val grade: Double,
		val maxGrade: Double
	) : EvaluationsDestination()
}
