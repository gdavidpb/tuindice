package com.gdavidpb.tuindice.data.model.database

import com.google.firebase.Timestamp

data class QuarterEntity(
        val userId: String,
        val startDate: Timestamp,
        val endDate: Timestamp,
        val grade: Double,
        val gradeSum: Double,
        val status: Int
)