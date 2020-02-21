package com.gdavidpb.tuindice.domain.model

import java.util.*

data class Evaluation(
        val id: String,
        val sid: String,
        val subjectCode: String,
        val type: EvaluationType,
        val grade: Int,
        val maxGrade: Int,
        val date: Date,
        val notes: String,
        val isDone: Boolean
)