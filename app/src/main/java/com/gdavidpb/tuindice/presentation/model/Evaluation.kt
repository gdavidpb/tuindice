package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.EvaluationType
import java.util.*

data class Evaluation(
        val type: EvaluationType,
        val maxGrade: Int,
        val date: Date,
        val notes: String
)