package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.domain.model.EvaluationType
import java.util.*

data class EvaluationItem(
        val id: String,
        val sid: String,
        val type: EvaluationType,
        val grade: Int,
        val maxGrade: Int,
        val date: Date,
        val notes: String,
        val isDone: Boolean,
        val isLoading: Boolean
)