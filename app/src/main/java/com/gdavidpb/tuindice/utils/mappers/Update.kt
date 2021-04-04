package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.domain.usecase.request.UpdateQuarterRequest
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
        update = EvaluationUpdate(
                eid = id,
                type = type,
                grade = grade,
                maxGrade = maxGrade,
                date = date,
                notes = notes,
                isDone = isDone
        ),
        dispatchChanges = dispatchChanges
)

fun Quarter.toUpdateRequest(
        sid: String,
        grade: Int,
        dispatchChanges: Boolean,
) = UpdateQuarterRequest(
        qid = id,
        sid = sid,
        grade = grade,
        dispatchChanges = dispatchChanges
)

fun Evaluation.applyUpdate(update: EvaluationUpdate) = copy(
        type = update.type,
        grade = update.grade,
        maxGrade = update.maxGrade,
        date = update.date,
        notes = update.notes,
        isDone = update.isDone
)

fun Quarter.applyUpdate(update: QuarterUpdate) = copy(
        grade = update.grade,
        gradeSum = update.gradeSum
)