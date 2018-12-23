package com.gdavidpb.tuindice.data.model.service

data class DstQuarter(
        val period: DstPeriod,
        val subjects: List<DstSubject>,
        val grade: Double,
        val gradeSum: Double
)