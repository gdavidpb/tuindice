package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import java.util.*

fun Evaluation.toUpdateRequest(
        type: EvaluationType = this.type,
        grade: Double = this.grade,
        maxGrade: Double = this.maxGrade,
        date: Date = this.date,
        notes: String = this.notes,
        isDone: Boolean = this.isDone,
        dispatchChanges: Boolean
) = UpdateEvaluationRequest(
        id = id,
        type = type,
        grade = grade,
        maxGrade = maxGrade,
        date = date,
        notes = notes,
        isDone = isDone,
        dispatchChanges = dispatchChanges
)

fun Evaluation.applyRequest(request: UpdateEvaluationRequest) = copy(
        type = request.type,
        grade = request.grade,
        maxGrade = request.maxGrade,
        date = request.date,
        notes = request.notes,
        isDone = request.isDone
)