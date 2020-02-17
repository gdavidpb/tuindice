package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.EvaluationType
import java.util.*

data class NewEvaluation(
        val id: String,
        val sid: String,
        val type: EvaluationType,
        val maxGrade: Int,
        val date: Date,
        val notes: String
)