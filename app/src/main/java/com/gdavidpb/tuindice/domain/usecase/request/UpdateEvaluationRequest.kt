package com.gdavidpb.tuindice.domain.usecase.request

import com.gdavidpb.tuindice.domain.model.EvaluationType
import java.util.*

data class UpdateEvaluationRequest(
        val id: String,
        val type: EvaluationType,
        val grade: Double,
        val maxGrade: Double,
        val date: Date,
        val notes: String,
        val isDone: Boolean
)
