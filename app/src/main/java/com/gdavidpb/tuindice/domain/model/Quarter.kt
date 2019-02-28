package com.gdavidpb.tuindice.domain.model

import java.util.*

data class Quarter(
        val startDate: Date,
        val endDate: Date,
        val grade: Double,
        val gradeSum: Double,
        val status: Int
)