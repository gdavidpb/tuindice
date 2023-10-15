package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation

fun (AddEvaluation.Action.ClickDone).toAddEvaluationParams() =
	AddEvaluationParams(
		quarterId = "", // TODO
		subjectId = subject?.id,
		grade = 0.0, // TODO
		maxGrade = maxGrade,
		date = date,
		type = type,
		isCompleted = true
	)