package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.domain.usecase.param.ValidateAddEvaluationStep1Params
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation

fun (AddEvaluation.Action.ClickStep1Done).toValidateAddEvaluationStep1Params() =
	ValidateAddEvaluationStep1Params(
		subject = subject,
		type = type
	)