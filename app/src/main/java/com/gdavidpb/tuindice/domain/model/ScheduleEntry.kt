package com.gdavidpb.tuindice.domain.model

data class ScheduleEntry(
        val dayOfWeek: Int,
        val startAt: Int,
        val endAt: Int
)