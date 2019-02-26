package com.gdavidpb.tuindice.domain.model.service

data class DstQuarter(
        val period: DstPeriod,
        val grade: Double,
        val gradeSum: Double,
        val subjects: List<DstSubject> = listOf()
)