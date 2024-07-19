package com.gdavidpb.tuindice.evaluations.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.serialization.Serializable

@Serializable
sealed class EvaluationsDestination : Destination2() {
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
		val grade: Float?,
		val maxGrade: Float?
	) : EvaluationsDestination()

	@Serializable
	data class MaxGradePickerDialog(
		val grade: Float?
	) : EvaluationsDestination()

	@Serializable
	data class EvaluationGradePickerDialog(
		val evaluationId: String,
		val grade: Float,
		val maxGrade: Float
	) : EvaluationsDestination()
}