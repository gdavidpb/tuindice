package com.gdavidpb.tuindice.domain.model.service

import java.util.*

data class DstQuarter(
        val startDate: Date,
        val endDate: Date,
        val grade: Double,
        val gradeSum: Double,
        val status: Int,
        val subjects: List<DstSubject> = listOf()
)