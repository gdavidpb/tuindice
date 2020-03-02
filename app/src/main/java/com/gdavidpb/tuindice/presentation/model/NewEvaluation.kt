package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.EvaluationType
import java.util.*

data class NewEvaluation(
        val id: String,
        val sid: String,
        val subjectCode: String,
        val type: EvaluationType,
        val maxGrade: Double,
        val date: Date,
        val notes: String
) {
    fun isNew() = id.isEmpty()
}