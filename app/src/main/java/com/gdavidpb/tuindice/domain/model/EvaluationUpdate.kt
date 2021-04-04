package com.gdavidpb.tuindice.domain.model

import java.util.*

data class EvaluationUpdate(
        val eid: String,
        val type: EvaluationType,
        val grade: Double,
        val maxGrade: Double,
        val date: Date,
        val notes: String,
        val isDone: Boolean,
)