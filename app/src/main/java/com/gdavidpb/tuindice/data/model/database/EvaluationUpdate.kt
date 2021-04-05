package com.gdavidpb.tuindice.data.model.database

import com.google.firebase.Timestamp

data class EvaluationUpdate(
        val type: Int,
        val grade: Double,
        val maxGrade: Double,
        val date: Timestamp,
        val notes: String,
        val isDone: Boolean,
)