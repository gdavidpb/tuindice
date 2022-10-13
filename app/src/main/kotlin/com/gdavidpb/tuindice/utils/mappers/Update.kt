package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.base.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.google.firebase.Timestamp
import java.util.*

fun Evaluation.toUpdateRequest(
	type: EvaluationType = this.type,
	grade: Double = this.grade,
	maxGrade: Double = this.maxGrade,
	date: Date = this.date,
	notes: String = this.notes,
	isDone: Boolean = this.isDone,
	dispatchChanges: Boolean,
) = UpdateEvaluationRequest(
        eid = id,
        update = EvaluationUpdate(
                type = type.ordinal,
                grade = grade,
                maxGrade = maxGrade,
                date = Timestamp(date),
                notes = notes,
                isDone = isDone
        ),
        dispatchChanges = dispatchChanges
)

fun Evaluation.applyUpdate(update: EvaluationUpdate) = copy(
        type = EvaluationType.values()[update.type],
        grade = update.grade,
        maxGrade = update.maxGrade,
        date = update.date.toDate(),
        notes = update.notes,
        isDone = update.isDone
)