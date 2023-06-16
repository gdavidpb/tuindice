package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

sealed class EvaluationsDialog : Dialog {
	class Filter(
		val filters: List<EvaluationFilter>
	) : EvaluationsDialog()
}