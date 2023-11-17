package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog

sealed class EvaluationDialog : Dialog {
	class GradePicker(val grade: Double?, val maxGrade: Double?) : EvaluationDialog()
	class MaxGradePicker(val grade: Double?) : EvaluationDialog()
}