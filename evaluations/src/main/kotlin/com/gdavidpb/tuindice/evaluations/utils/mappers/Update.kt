package com.gdavidpb.tuindice.evaluations.utils.mappers

import com.gdavidpb.tuindice.base.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.domain.param.UpdateEvaluationParams
import java.util.*

fun Evaluation.toUpdateRequest(
	type: EvaluationType = this.type,
	grade: Double = this.grade,
	maxGrade: Double = this.maxGrade,
	date: Date = this.date,
	notes: String = this.notes,
	isDone: Boolean = this.isDone,
	dispatchChanges: Boolean,
) = UpdateEvaluationParams(
	eid = id,
	update = EvaluationUpdate(
		type = type.ordinal,
		grade = grade,
		maxGrade = maxGrade,
		date = date,
		notes = notes,
		isDone = isDone
	),
	dispatchChanges = dispatchChanges
)

fun Evaluation.applyUpdate(update: EvaluationUpdate) = copy(
	type = EvaluationType.values()[update.type],
	grade = update.grade,
	maxGrade = update.maxGrade,
	date = update.date,
	notes = update.notes,
	isDone = update.isDone
)