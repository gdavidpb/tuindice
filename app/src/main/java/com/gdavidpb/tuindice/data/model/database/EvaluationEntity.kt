package com.gdavidpb.tuindice.data.model.database

import java.util.*

data class EvaluationEntity(
        val id: String,
        val sid: String,
        val type: Int,
        val grade: Int,
        val maxGrade: Int,
        val date: Date,
        val notes: String,
        val done: Boolean
)