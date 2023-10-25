package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog

sealed class EvaluationsDialog : Dialog {
	class RemoveEvaluationDialog(
		val evaluationId: String,
		val message: String
	) : EvaluationsDialog()
}