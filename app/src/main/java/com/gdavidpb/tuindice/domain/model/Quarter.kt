package com.gdavidpb.tuindice.domain.model

data class Quarter(
        val period: Period,
        val subjects: List<Subject>,
        val grade: Double,
        val gradeSum: Double
)