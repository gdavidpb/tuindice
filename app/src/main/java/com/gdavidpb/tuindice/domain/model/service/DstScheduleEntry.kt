package com.gdavidpb.tuindice.domain.model.service

data class DstScheduleEntry(
        val dayOfWeek: Int,
        val startAt: Int,
        val endAt: Int,
        val classroom: String
)