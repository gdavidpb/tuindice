package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.domain.usecase.param.ValidateAddEvaluationStep1Params
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation

fun (AddEvaluation.Action.ClickStep1Done).toValidateAddEvaluationStep1Params() =
	ValidateAddEvaluationStep1Params(
		subject = subject,
		type = type
	)

fun (AddEvaluation.State.Step1).toStep2() =
	AddEvaluation.State.Step2(
		availableSubjects = availableSubjects,
		subject = subject!!,
		type = type!!
	)

fun (AddEvaluation.State.Step2).toStep1() =
	AddEvaluation.State.Step1(
		availableSubjects = availableSubjects,
		subject = subject,
		type = type
	)

fun (AddEvaluation.State).toStep1() =
	(this as? AddEvaluation.State.Loading)?.state as? AddEvaluation.State.Step1