package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog

sealed class EvaluationsDialog : Dialog {
	class GradePicker(
		val evaluationId: String,
		val grade: Double,
		val maxGrade: Double
	) : EvaluationsDialog()
}