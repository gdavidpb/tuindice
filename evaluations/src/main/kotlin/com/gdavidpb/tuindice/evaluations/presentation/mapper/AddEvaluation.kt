package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation

fun (AddEvaluation.Action.ClickDone).toAddEvaluationParams() =
	AddEvaluationParams(
		quarterId = subject?.qid,
		subjectId = subject?.id,
		grade = grade ?: 0.0,
		maxGrade = maxGrade,
		date = date,
		type = type
	)