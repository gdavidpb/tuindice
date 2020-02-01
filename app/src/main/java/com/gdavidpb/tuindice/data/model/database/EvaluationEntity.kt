package com.gdavidpb.tuindice.data.model.database

import com.google.firebase.Timestamp

data class EvaluationEntity(
        val userId: String,
        val subjectId: String,
        val type: Int,
        val grade: Int,
        val maxGrade: Int,
        val date: Timestamp,
        val notes: String,
        val isDone: Boolean
)