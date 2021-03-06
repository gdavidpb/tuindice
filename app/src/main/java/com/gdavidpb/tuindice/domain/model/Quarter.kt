package com.gdavidpb.tuindice.domain.model

import java.util.*

data class Quarter(
        val id: String,
        val name: String,
        val startDate: Date,
        val endDate: Date,
        val grade: Double,
        val gradeSum: Double,
        val credits: Int,
        val status: Int,
        val subjects: MutableList<Subject>
)