package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations

fun (Evaluation.Action.LoadEvaluation).toGetEvaluationParams() =
	GetEvaluationParams(
		evaluationId = evaluationId
	)

fun (Evaluation.Action.ClickAddEvaluation).toAddEvaluationParams() =
	AddEvaluationParams(
		quarterId = subject?.qid,
		subjectId = subject?.id,
		grade = grade ?: 0.0,
		maxGrade = maxGrade,
		date = date,
		type = type
	)

fun (Evaluation.Action.ClickEditEvaluation).toUpdateEvaluationParams() =
	UpdateEvaluationParams(
		evaluationId = evaluationId,
		subjectId = subject?.id,
		grade = grade ?: 0.0,
		maxGrade = maxGrade,
		date = date,
		type = type
	)

fun (Evaluations.Action.SetEvaluationGrade).toUpdateEvaluationParams() =
	UpdateEvaluationParams(
		evaluationId = evaluationId,
		grade = grade
	)