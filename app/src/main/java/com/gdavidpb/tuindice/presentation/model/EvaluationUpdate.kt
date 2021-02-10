package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.EvaluationType
import java.util.*

data class EvaluationUpdate(
        val type: EvaluationType,
        val maxGrade: Double,
        val date: Date,
        val notes: String
)